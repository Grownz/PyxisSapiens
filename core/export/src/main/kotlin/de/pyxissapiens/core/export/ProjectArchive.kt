package de.pyxissapiens.core.export

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.security.SecureRandom
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Portable project archive (`.pyxis`): a ZIP bundle that is optionally encrypted with
 * AES-GCM using a PBKDF2-derived key. Layout when encrypted:
 * `MAGIC(6) | salt(16) | iv(12) | ciphertext`.
 */
object ProjectArchive {

    private val MAGIC = "PYXIS1".toByteArray(Charsets.US_ASCII)
    private const val SALT_LEN = 16
    private const val IV_LEN = 12
    private const val ITERATIONS = 100_000
    private const val KEY_BITS = 256
    private const val TAG_BITS = 128

    fun write(files: Map<String, ByteArray>, password: String?): ByteArray {
        val zip = zip(files)
        return if (password.isNullOrEmpty()) zip else encrypt(zip, password)
    }

    fun read(bytes: ByteArray, password: String?): Map<String, ByteArray> {
        val encrypted = bytes.size > MAGIC.size && bytes.copyOfRange(0, MAGIC.size).contentEquals(MAGIC)
        val zip = if (encrypted) {
            val pw = password ?: error("Password required for encrypted archive")
            decrypt(bytes, pw)
        } else {
            bytes
        }
        return unzip(zip)
    }

    fun isEncrypted(bytes: ByteArray): Boolean =
        bytes.size > MAGIC.size && bytes.copyOfRange(0, MAGIC.size).contentEquals(MAGIC)

    private fun zip(files: Map<String, ByteArray>): ByteArray {
        val baos = ByteArrayOutputStream()
        ZipOutputStream(baos).use { zip ->
            for ((name, content) in files) {
                zip.putNextEntry(ZipEntry(name))
                zip.write(content)
                zip.closeEntry()
            }
        }
        return baos.toByteArray()
    }

    private fun unzip(bytes: ByteArray): Map<String, ByteArray> {
        val result = LinkedHashMap<String, ByteArray>()
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                result[entry.name] = zip.readBytes()
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }
        return result
    }

    private fun encrypt(plain: ByteArray, password: String): ByteArray {
        val salt = ByteArray(SALT_LEN).also { SecureRandom().nextBytes(it) }
        val iv = ByteArray(IV_LEN).also { SecureRandom().nextBytes(it) }
        val key = deriveKey(password, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(TAG_BITS, iv))
        val cipherText = cipher.doFinal(plain)
        val out = ByteArrayOutputStream()
        out.write(MAGIC)
        out.write(salt)
        out.write(iv)
        out.write(cipherText)
        return out.toByteArray()
    }

    private fun decrypt(bytes: ByteArray, password: String): ByteArray {
        var offset = MAGIC.size
        val salt = bytes.copyOfRange(offset, offset + SALT_LEN); offset += SALT_LEN
        val iv = bytes.copyOfRange(offset, offset + IV_LEN); offset += IV_LEN
        val cipherText = bytes.copyOfRange(offset, bytes.size)
        val key = deriveKey(password, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(TAG_BITS, iv))
        return cipher.doFinal(cipherText)
    }

    private fun deriveKey(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_BITS)
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
    }
}
