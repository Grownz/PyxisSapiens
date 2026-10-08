package de.pyxissapiens.core.database.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.pyxissapiens.core.database.PyxisDatabase
import de.pyxissapiens.core.database.crypto.DatabaseEncryption
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PyxisDatabase {
        // sqlcipher-android ships a native library that must be loaded before use.
        System.loadLibrary("sqlcipher")

        // Resolves the passphrase matching the current file (handles first-time encryption and
        // transparent re-keying when the optional user passphrase changed).
        val passphrase = DatabaseEncryption.resolvePassphrase(context)

        return Room.databaseBuilder(context, PyxisDatabase::class.java, "pyxis.db")
            .openHelperFactory(SupportOpenHelperFactory(passphrase))
            .addMigrations(*PyxisDatabase.ALL)
            .build()
    }
}
