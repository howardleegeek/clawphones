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

        val secret = AuthenticationService.getMfaSecret("alice")
        assertNotNull("MFA secret should exist for alice", secret)
        val otp = OtpUtil.generateOtp(secret!!)

        val loginWithOtp = AuthenticationService.login("alice", "password123", otp)
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
}
