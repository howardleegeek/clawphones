package com.clawphones.security

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class AuthenticationManagerTest {

    @Test
    fun testRegisterUser() {
        val result = AuthenticationManager.registerUser("testuser", "password123")
        assertTrue(result)
    }

    @Test
    fun testRegisterDuplicateUser() {
        AuthenticationManager.registerUser("testuser2", "password123")
        val result = AuthenticationManager.registerUser("testuser2", "password456")
        assertFalse(result)
    }

    @Test
    fun testAuthenticateWithoutMfa() {
        AuthenticationManager.registerUser("user1", "pass123")
        val result = AuthenticationManager.authenticate("user1", "pass123")
        assertTrue(result.success)
        assertFalse(result.requiresMfa)
    }

    @Test
    fun testAuthenticateInvalidCredentials() {
        AuthenticationManager.registerUser("user2", "pass123")
        val result = AuthenticationManager.authenticate("user2", "wrongpass")
        assertFalse(result.success)
    }

    @Test
    fun testMfaEnrollment() {
        AuthenticationManager.registerUser("user3", "pass123")
        val secret = AuthenticationManager.enrollMfa("user3")
        assertNotNull(secret)
        assertTrue(secret!!.length >= 16)
    }

    @Test
    fun testEnableMfa() {
        AuthenticationManager.registerUser("user4", "pass123")
        val secret = AuthenticationManager.enrollMfa("user4")
        assertNotNull(secret)

        val currentOtp = OtpUtil.generateTotp(secret!!)
        val enabled = AuthenticationManager.enableMfa("user4", currentOtp)
        assertTrue(enabled)
        assertTrue(AuthenticationManager.isMfaEnabled("user4"))
    }

    @Test
    fun testMfaRequiredAfterEnabled() {
        AuthenticationManager.registerUser("user5", "pass123")
        val secret = AuthenticationManager.enrollMfa("user5")
        val currentOtp = OtpUtil.generateTotp(secret!!)
        AuthenticationManager.enableMfa("user5", currentOtp)

        val result = AuthenticationManager.authenticate("user5", "pass123")
        assertFalse(result.success)
        assertTrue(result.requiresMfa)
    }

    @Test
    fun testVerifyMfaSuccess() {
        AuthenticationManager.registerUser("user6", "pass123")
        val secret = AuthenticationManager.enrollMfa("user6")
        val currentOtp = OtpUtil.generateTotp(secret!!)
        AuthenticationManager.enableMfa("user6", currentOtp)

        AuthenticationManager.authenticate("user6", "pass123")
        val mfaResult = AuthenticationManager.verifyMfa("user6", currentOtp)
        assertTrue(mfaResult.success)
    }

    @Test
    fun testVerifyMfaFailure() {
        AuthenticationManager.registerUser("user7", "pass123")
        val secret = AuthenticationManager.enrollMfa("user7")
        val currentOtp = OtpUtil.generateTotp(secret!!)
        AuthenticationManager.enableMfa("user7", currentOtp)

        AuthenticationManager.authenticate("user7", "pass123")
        val mfaResult = AuthenticationManager.verifyMfa("user7", "000000")
        assertFalse(mfaResult.success)
    }

    @Test
    fun testDisableMfa() {
        AuthenticationManager.registerUser("user8", "pass123")
        val secret = AuthenticationManager.enrollMfa("user8")
        val currentOtp = OtpUtil.generateTotp(secret!!)
        AuthenticationManager.enableMfa("user8", currentOtp)

        assertTrue(AuthenticationManager.isMfaEnabled("user8"))

        val disabled = AuthenticationManager.disableMfa("user8")
        assertTrue(disabled)
        assertFalse(AuthenticationManager.isMfaEnabled("user8"))
    }
}
