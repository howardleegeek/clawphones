package com.clawphones.app.security

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class CryptoManagerTest {

    @Test
    fun roundTripEncryptionDecryption() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        CryptoManager.init(context)
        val plain = "SensitiveData123!"
        val enc = CryptoManager.encrypt(plain)
        val dec = CryptoManager.decrypt(enc)
        assertEquals(plain, dec)
    }

    @Test
    fun nonDeterministicEncryptionProducesDifferentCiphertexts() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        CryptoManager.init(context)
        val a = CryptoManager.encrypt("same")
        val b = CryptoManager.encrypt("same")
        assertFalse(a.ciphertext == b.ciphertext)
        assertFalse(a.iv == b.iv)
    }

    @Test
    fun decryptWithTamperedCipherThrows() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        CryptoManager.init(context)
        val enc = CryptoManager.encrypt("data")
        val tampered = enc.copy(ciphertext = enc.ciphertext + "A")
        try {
            CryptoManager.decrypt(tampered)
            fail("Expected decryption to fail for tampered data")
        } catch (e: Exception) {
            // expected
        }
    }
}
