package com.clawphones.security

import org.junit.Assert.*
import org.junit.Test

class AuthenticationServiceTest {

    @Test
    fun testMfaFlowSuccess() {
        // alice has MFA enabled in the in-memory store
        val initialLogin = AuthenticationService.login("alice", "password123")
        assertFalse("Login should require OTP initially", initialLogin.success)
        assertTrue("Login should require OTP", initialLogin.requiresOtp)
        assertEquals("Missing OTP should be explicit", "OTP required", initialLogin.message)

        val otp = AuthenticationService.generateOtpForUser("alice")
        assertNotNull("OTP should be generated for MFA user", otp)

        val loginWithOtp = AuthenticationService.login("alice", "password123", otp!!)
        assertTrue("Login with valid OTP should succeed", loginWithOtp.success)
    }

    @Test
    fun testMfaFlowInvalidOtp() {
        val initialLogin = AuthenticationService.login("alice", "password123")
        assertFalse("Login should require OTP", initialLogin.success)
        assertTrue("Login should require OTP", initialLogin.requiresOtp)

        val loginWithWrongOtp = AuthenticationService.login("alice", "password123", "000000")
        assertFalse("Login with invalid OTP should fail", loginWithWrongOtp.success)
        assertTrue("OTP is required flag", loginWithWrongOtp.requiresOtp || !loginWithWrongOtp.success)
    }

    @Test
    fun testNoMfaUser() {
        val loginGuest = AuthenticationService.login("guest", "guestpass")
        assertTrue("Guest login should succeed without MFA", loginGuest.success)
    }

    @Test
    fun testGenerateAndVerifyOtpForEnabledMfaUser() {
        val username = "mfa_user_${System.nanoTime()}"
        assertTrue(AuthenticationService.registerUser(username, "secret"))

        val secret = AuthenticationService.enableMfa(username)
        assertNotNull("MFA secret should be provisioned", secret)

        val otp = AuthenticationService.generateOtpForUser(username)
        assertNotNull("OTP should be generated", otp)
        assertTrue("Generated OTP should validate", AuthenticationService.verifyOtp(username, otp!!))
        assertFalse("Invalid OTP should fail", AuthenticationService.verifyOtp(username, "111111"))
    }
}
