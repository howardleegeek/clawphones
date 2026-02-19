package com.clawphones.security

import java.util.concurrent.ConcurrentHashMap

/** Simple in-memory authentication service with optional MFA (OTP). */
object AuthenticationService {
    private const val OTP_STEP_MILLIS = 30_000L
    private const val OTP_DRIFT_STEPS = 1

    data class User(
        val username: String,
        val password: String,
        var mfaSecret: String? = null,
        var mfaEnabled: Boolean = false
    )

    data class LoginResult(val success: Boolean, val requiresOtp: Boolean = false, val message: String? = null)

    private val users = ConcurrentHashMap<String, User>()

    init {
        // Seed with sample users
        // User without MFA
        val guest = User(username = "guest", password = "guestpass", mfaEnabled = false)
        users[guest.username] = guest

        // User with MFA enabled
        val secret = OtpUtil.generateSecret()
        val alice = User(username = "alice", password = "password123", mfaSecret = secret, mfaEnabled = true)
        users[alice.username] = alice
    }

    // Register a new user (no MFA by default)
    fun registerUser(username: String, password: String): Boolean {
        if (users.containsKey(username)) return false
        users[username] = User(username = username, password = password)
        return true
    }

    // Enable MFA for an existing user and return the secret for provisioning
    fun enableMfa(username: String): String? {
        val user = users[username] ?: return null
        val secret = OtpUtil.generateSecret()
        user.mfaSecret = secret
        user.mfaEnabled = true
        return secret
    }

    fun getMfaSecret(username: String): String? = users[username]?.mfaSecret

    fun isMfaEnabled(username: String): Boolean {
        return users[username]?.mfaEnabled ?: false
    }

    fun generateOtpForUser(username: String, timestamp: Long = System.currentTimeMillis()): String? {
        val user = users[username] ?: return null
        val secret = user.mfaSecret ?: return null
        return OtpUtil.generateOtp(secret, timestamp)
    }

    fun verifyOtp(username: String, otp: String, timestamp: Long = System.currentTimeMillis()): Boolean {
        val user = users[username] ?: return false
        if (!user.mfaEnabled) return false
        val secret = user.mfaSecret ?: return false
        return isOtpValid(secret, otp, timestamp)
    }

    private fun isOtpValid(secret: String, otp: String, timestamp: Long): Boolean {
        for (step in -OTP_DRIFT_STEPS..OTP_DRIFT_STEPS) {
            val candidate = OtpUtil.generateOtp(secret, timestamp + (step * OTP_STEP_MILLIS))
            if (candidate == otp) return true
        }
        return false
    }

    /** Attempt to login with username and password. If MFA is enabled for the user,
     *  otp must be provided and valid. */
    fun login(username: String, password: String, otp: String? = null): LoginResult {
        val user = users[username] ?: return LoginResult(false, message = "User not found")
        if (user.password != password) {
            return LoginResult(false, message = "Invalid credentials")
        }
        if (user.mfaEnabled) {
            if (otp == null) return LoginResult(false, requiresOtp = true, message = "OTP required")
            val secret = user.mfaSecret ?: return LoginResult(false, message = "MFA secret missing")
            val valid = isOtpValid(secret, otp, System.currentTimeMillis())
            return if (valid) LoginResult(true) else LoginResult(false, requiresOtp = true, message = "Invalid OTP")
        }
        // MFA not enabled; login success
        return LoginResult(true)
    }
}
