package de.pyxissapiens.core.database.crypto

import android.content.Context
import androidx.room.Room
import de.pyxissapiens.core.database.PyxisDatabase
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import java.io.File
/**
 * Establishes and maintains database encryption using Room on both sides (no low-level SQLCipher
 * API):
 *
 *  - fresh install: the encrypted database is created on first open;
 *  - existing plaintext database: its rows are copied into a new encrypted database once;
 *  - changed user passphrase: the database is transparently re-keyed (old key -> new key).
 *
 * [resolvePassphrase] returns the passphrase that matches how the file is currently encrypted, so
 * the caller always opens the database with a working key even if a re-key fails.
 */
object DatabaseEncryption {

    private const val DB_NAME = "pyxis.db"
    private const val PLAIN_NAME = "pyxis_plain_migrate.db"
    private const val REKEY_NAME = "pyxis_rekey.db"
    private const val MARKER = "pyxis.encrypted"

    fun resolvePassphrase(context: Context): ByteArray {
        val currentUser = PassphraseStore.userValue(context)
        val newPass = PassphraseStore.passphraseFor(context, currentUser)

        val dbFile = context.getDatabasePath(DB_NAME)
        val marker = File(dbFile.parentFile, MARKER)

        if (!dbFile.exists()) {
            dbFile.parentFile?.mkdirs()
            marker.createNewFile()
            PassphraseStore.markApplied(context, currentUser)
            return newPass
        }

        if (!marker.exists()) {
            runCatching { migratePlaintext(context, newPass) }
                .onFailure {
                    context.deleteDatabase(PLAIN_NAME)
                    context.deleteDatabase(DB_NAME)
                }
            marker.createNewFile()
            PassphraseStore.markApplied(context, currentUser)
            return newPass
        }

        val applied = PassphraseStore.appliedUserValue(context)
        if (applied != currentUser) {
            val oldPass = PassphraseStore.passphraseFor(context, applied)
            val rekeyed = runCatching { rekey(context, oldPass, newPass) }.isSuccess
            if (rekeyed) {
                PassphraseStore.markApplied(context, currentUser)
                return newPass
            }
            // Re-key failed: keep using the key that actually opens the file.
            return oldPass
        }
        return newPass
    }

    private fun factory(passphrase: ByteArray) = SupportOpenHelperFactory(passphrase)

    private fun builder(context: Context, name: String, passphrase: ByteArray) =
        Room.databaseBuilder(context, PyxisDatabase::class.java, name)
            .openHelperFactory(factory(passphrase))
            .addMigrations(*PyxisDatabase.ALL)
            .build()

    private fun migratePlaintext(context: Context, passphrase: ByteArray) {
        val dbFile = context.getDatabasePath(DB_NAME)
        val plainFile = context.getDatabasePath(PLAIN_NAME)
        plainFile.delete()
        dbFile.renameTo(plainFile)
        kotlinx.coroutines.runBlocking {
            val plain = Room.databaseBuilder(context, PyxisDatabase::class.java, PLAIN_NAME).build()
            val encrypted = builder(context, DB_NAME, passphrase)
            copy(plain, encrypted)
            plain.close()
            encrypted.close()
        }
        context.deleteDatabase(PLAIN_NAME)
    }

    private fun rekey(context: Context, oldPassphrase: ByteArray, newPassphrase: ByteArray) {
        context.deleteDatabase(REKEY_NAME)
        kotlinx.coroutines.runBlocking {
            val old = builder(context, DB_NAME, oldPassphrase)
            val fresh = builder(context, REKEY_NAME, newPassphrase)
            copy(old, fresh)
            old.close()
            fresh.close()
        }
        // Swap the re-keyed file into place.
        val target = context.getDatabasePath(DB_NAME)
        val source = context.getDatabasePath(REKEY_NAME)
        listOf("", "-wal", "-shm").forEach { suffix ->
            File(target.absolutePath + suffix).delete()
        }
        source.renameTo(target)
    }

    private suspend fun copy(from: PyxisDatabase, to: PyxisDatabase) {
        from.projectDao().getAllOnce().forEach { to.projectDao().upsert(it) }
        from.siteDao().getAllOnce().forEach { to.siteDao().upsert(it) }
        from.measurementDao().getAllOnce().forEach { to.measurementDao().upsert(it) }
        from.measurementHistoryDao().getAllOnce().forEach { to.measurementHistoryDao().append(it) }
        from.lineworkDao().getAllOnce().forEach { to.lineworkDao().upsert(it) }
        from.trackDao().getAllOnce().forEach { to.trackDao().upsert(it) }
        from.dataTypeDao().getAllOnce().forEach { to.dataTypeDao().upsert(it) }
        from.unitDao().getAllOnce().forEach { to.unitDao().upsert(it) }
    }
}
