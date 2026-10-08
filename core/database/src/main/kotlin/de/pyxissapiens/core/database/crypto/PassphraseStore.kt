package de.pyxissapiens.core.database.crypto

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec

/**
 * Provides the SQLCipher passphrase.
 *
 * A random 256-bit master key is generated once and wrapped with an Android Keystore AES key
 * (transparent, no user input). If the user has set an optional passphrase, the database key is
 * derived from it via PBKDF2 using the master key as salt.
 */
object PassphraseStore {

    private const val PREFS = "pyxis_secure"
    private const val KEY_WRAP = "wrapped_master_key"
    private const val KEY_USER = "user_passphrase"
    private const val KEY_APPLIED = "applied_user_passphrase"
    private const val ALIAS = "pyxis_db_master_key"
    private const val GCM_TAG_BITS = 128

    fun userValue(context: Context): String? = prefs(context).getString(KEY_USER, null)

    fun appliedUserValue(context: Context): String? = prefs(context).getString(KEY_APPLIED, null)

    fun markApplied(context: Context, value: String?) {
        prefs(context).edit().putString(KEY_APPLIED, value).apply()
    }

    fun setUserPassphrase(context: Context, value: String?) {
        prefs(context).edit().putString(KEY_USER, value?.takeIf { it.isNotEmpty() }).apply()
    }

    fun hasUserPassphrase(context: Context): Boolean = !userValue(context).isNullOrEmpty()

    fun passphrase(context: Context): ByteArray = passphraseFor(context, userValue(context))

    fun passphraseFor(context: Context, userValue: String?): ByteArray {
        val master = masterKey(context)
        return if (userValue.isNullOrEmpty()) master else derive(userValue, master)
    }

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun masterKey(context: Context): ByteArray {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val secretKey: SecretKey = if (keyStore.containsAlias(ALIAS)) {
            (keyStore.getEntry(ALIAS, null) as KeyStore.SecretKeyEntry).secretKey
        } else {
            val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            generator.init(
                KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build(),
            )
            generator.generateKey()
        }

        val wrapped = prefs(context).getString(KEY_WRAP, null)
        if (wrapped != null) {
            val bytes = Base64.decode(wrapped, Base64.NO_WRAP)
            val iv = bytes.copyOfRange(0, 12)
            val cipherText = bytes.copyOfRange(12, bytes.size)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_BITS, iv))
            return cipher.doFinal(cipherText)
        }

        val random = ByteArray(32).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val cipherText = cipher.doFinal(random)
        prefs(context).edit()
            .putString(KEY_WRAP, Base64.encodeToString(cipher.iv + cipherText, Base64.NO_WRAP))
            .apply()
        return random
    }

    private fun derive(userPassphrase: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(userPassphrase.toCharArray(), salt.copyOfRange(0, 16), 100_000, 256)
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
    }
}
