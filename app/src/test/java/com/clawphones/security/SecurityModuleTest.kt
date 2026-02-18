package com.clawphones.security

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SecurityModuleTest {
    @Test
    fun encryptionRoundTrip() {
        val password = "test-password-1234"
        val module = SecurityModule(password)
        val plaintext = "Sensitive user data".toByteArray(Charsets.UTF_8)

        val encrypted = module.encrypt(plaintext)
        val decrypted = module.decrypt(encrypted)

        assertArrayEquals(plaintext, decrypted)
    }

    @Test
    fun mfaTotpValid() {
        val seed = "MFASharedSecretSeed".toByteArray(Charsets.UTF_8)
        val mfa = MfaService(seed)
        val fixedTime = 0L // epoch
        val code = mfa.generateTotp(fixedTime)
        assertTrue("TOTPs should verify at fixed time", mfa.verifyTotp(code, fixedTime))
    }

    @Test
    fun mfaTotpInvalid() {
        val seed = "AnotherSeed".toByteArray(Charsets.UTF_8)
        val mfa = MfaService(seed)
        val fakeCode = "000000"
        assertFalse("Fake code should not verify", mfa.verifyTotp(fakeCode))
    }
}
