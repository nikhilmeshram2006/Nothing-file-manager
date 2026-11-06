package com.example

import com.example.crypto.VaultCryptoManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testVaultEncryptionAndDecryption() {
        val originalText = "NOTHING_OS_SECRET_KEY_2026"
        val pin = "1920"

        val encryptedBase64 = VaultCryptoManager.encryptString(originalText, pin)
        assertNotNull(encryptedBase64)
        assertNotEquals(originalText, encryptedBase64)

        val decryptedText = VaultCryptoManager.decryptString(encryptedBase64, pin)
        assertEquals(originalText, decryptedText)
    }

    @Test
    fun testVaultBytesEncryption() {
        val testBytes = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x55)
        val pin = "8492"

        val encrypted = VaultCryptoManager.encrypt(testBytes, pin)
        assertNotNull(encrypted)

        val decrypted = VaultCryptoManager.decrypt(encrypted, pin)
        assertEquals(testBytes.size, decrypted.size)
        for (i in testBytes.indices) {
            assertEquals(testBytes[i], decrypted[i])
        }
    }
}
