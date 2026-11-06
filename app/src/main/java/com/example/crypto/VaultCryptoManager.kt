package com.example.crypto

import android.content.Context
import android.util.Base64
import java.io.File
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object VaultCryptoManager {
    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val TAG_LENGTH_BIT = 128
    private const val IV_LENGTH_BYTE = 12
    private const val SALT_LENGTH_BYTE = 16
    private const val ITERATION_COUNT = 10000
    private const val KEY_LENGTH_BIT = 256

    private val secureRandom = SecureRandom()

    fun getVaultDirectory(context: Context): File {
        val dir = File(context.filesDir, "encrypted_vault")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    private fun deriveKey(passcode: String, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(passcode.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH_BIT)
        val secretKey = factory.generateSecret(spec)
        return SecretKeySpec(secretKey.encoded, "AES")
    }

    fun encrypt(data: ByteArray, passcode: String): ByteArray {
        val salt = ByteArray(SALT_LENGTH_BYTE).apply { secureRandom.nextBytes(this) }
        val iv = ByteArray(IV_LENGTH_BYTE).apply { secureRandom.nextBytes(this) }

        val key = deriveKey(passcode, salt)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(TAG_LENGTH_BIT, iv))

        val cipherText = cipher.doFinal(data)

        // Structure: [salt (16b)][iv (12b)][cipherText (N bytes)]
        val combined = ByteArray(salt.size + iv.size + cipherText.size)
        System.arraycopy(salt, 0, combined, 0, salt.size)
        System.arraycopy(iv, 0, combined, salt.size, iv.size)
        System.arraycopy(cipherText, 0, combined, salt.size + iv.size, cipherText.size)

        return combined
    }

    fun decrypt(encryptedData: ByteArray, passcode: String): ByteArray {
        require(encryptedData.size >= SALT_LENGTH_BYTE + IV_LENGTH_BYTE) { "Invalid payload size" }

        val salt = ByteArray(SALT_LENGTH_BYTE)
        val iv = ByteArray(IV_LENGTH_BYTE)
        val cipherTextLength = encryptedData.size - SALT_LENGTH_BYTE - IV_LENGTH_BYTE
        val cipherText = ByteArray(cipherTextLength)

        System.arraycopy(encryptedData, 0, salt, 0, SALT_LENGTH_BYTE)
        System.arraycopy(encryptedData, SALT_LENGTH_BYTE, iv, 0, IV_LENGTH_BYTE)
        System.arraycopy(encryptedData, SALT_LENGTH_BYTE + IV_LENGTH_BYTE, cipherText, 0, cipherTextLength)

        val key = deriveKey(passcode, salt)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_LENGTH_BIT, iv))

        return cipher.doFinal(cipherText)
    }

    fun encryptString(text: String, passcode: String): String {
        val bytes = text.toByteArray(Charsets.UTF_8)
        val encrypted = encrypt(bytes, passcode)
        return java.util.Base64.getEncoder().encodeToString(encrypted)
    }

    fun decryptString(base64Payload: String, passcode: String): String {
        val bytes = java.util.Base64.getDecoder().decode(base64Payload)
        val decrypted = decrypt(bytes, passcode)
        return String(decrypted, Charsets.UTF_8)
    }
}
