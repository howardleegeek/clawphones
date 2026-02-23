package com.clawphones.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class SecurityModuleTest {

    private val securityModule = SecurityModule("G12-05-CP")

    @Test
    fun testEncryptAndDecryptBasicString() {
        val plainText = "Hello, World!"
        val encrypted = securityModule.encrypt(plainText)
        val decrypted = securityModule.decrypt(encrypted)

        assertEquals(plainText, decrypted)
    }

    @Test
    fun testEncryptProducesDifferentOutput() {
        val plainText = "SensitiveData123"
        val encrypted1 = securityModule.encrypt(plainText)
        val encrypted2 = securityModule.encrypt(plainText)

        assertNotEquals(encrypted1, encrypted2)
    }

    @Test
    fun testEncryptEmptyString() {
        val plainText = ""
        val encrypted = securityModule.encrypt(plainText)
        val decrypted = securityModule.decrypt(encrypted)

        assertEquals(plainText, decrypted)
    }

    @Test
    fun testEncryptAndDecryptAuthToken() {
        val authToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.testtoken"
        val encrypted = securityModule.encrypt(authToken)
        val decrypted = securityModule.decrypt(encrypted)

        assertEquals(authToken, decrypted)
    }

    @Test
    fun testEncryptAndDecryptApiKey() {
        val apiKey = "sk-1234567890abcdefghijklmnopqrstuvwxyz"
        val encrypted = securityModule.encrypt(apiKey)
        val decrypted = securityModule.decrypt(encrypted)

        assertEquals(apiKey, decrypted)
    }

    @Test
    fun testEncryptAndDecryptUnicode() {
        val plainText = "用户数据🔐加密"
        val encrypted = securityModule.encrypt(plainText)
        val decrypted = securityModule.decrypt(encrypted)

        assertEquals(plainText, decrypted)
    }

    @Test
    fun testEncryptAndDecryptLongString() {
        val plainText = "A".repeat(10000)
        val encrypted = securityModule.encrypt(plainText)
        val decrypted = securityModule.decrypt(encrypted)

        assertEquals(plainText, decrypted)
    }

    @Test
    fun testEncryptAndDecryptSpecialCharacters() {
        val plainText = "!@#\$%^&*()_+-=[]{}|;':\",./<>?`~"
        val encrypted = securityModule.encrypt(plainText)
        val decrypted = securityModule.decrypt(encrypted)

        assertEquals(plainText, decrypted)
    }
}
