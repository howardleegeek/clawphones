package com.clawphones.security

import java.security.SecureRandom
import java.util.concurrent.ConcurrentHashMap
import java.util.Base64

data class MfaConfig(
    val secret: String,
    val enabled: Boolean = false
)

data class AuthResult(
    val success: Boolean,
    val requiresMfa: Boolean = false,
    val message: String = "",
    val userId: String? = null
)

object AuthenticationManager {
    private val users = ConcurrentHashMap<String, String>()
    private val mfaConfigs = ConcurrentHashMap<String, MfaConfig>()
    private val mfaPendingUsers = ConcurrentHashMap<String, String>()

    fun registerUser(userId: String, password: String): Boolean {
        return users.putIfAbsent(userId, password) == null
    }

    fun authenticate(userId: String, password: String): AuthResult {
        val storedPassword = users[userId]
        if (storedPassword == null || storedPassword != password) {
            return AuthResult(success = false, message = "Invalid credentials")
        }

        val mfaConfig = mfaConfigs[userId]
        if (mfaConfig != null && mfaConfig.enabled) {
            mfaPendingUsers[userId] = password
            return AuthResult(success = false, requiresMfa = true, message = "MFA required", userId = userId)
        }

        return AuthResult(success = true, message = "Authentication successful", userId = userId)
    }

    fun verifyMfa(userId: String, otp: String): AuthResult {
        val password = mfaPendingUsers.remove(userId) ?: return AuthResult(success = false, message = "No MFA pending")

        val mfaConfig = mfaConfigs[userId]
        if (mfaConfig == null || !mfaConfig.enabled) {
            return AuthResult(success = false, message = "MFA not enabled")
        }

        if (OtpUtil.isOtpValid(otp, mfaConfig.secret)) {
            return AuthResult(success = true, message = "MFA authentication successful", userId = userId)
        }

        return AuthResult(success = false, message = "Invalid OTP")
    }

    fun enrollMfa(userId: String): String? {
        val password = users[userId] ?: return null

        val secret = generateSecret()
        mfaConfigs[userId] = MfaConfig(secret = secret, enabled = false)
        return secret
    }

    fun enableMfa(userId: String, otp: String): Boolean {
        val mfaConfig = mfaConfigs[userId] ?: return false

        if (OtpUtil.isOtpValid(otp, mfaConfig.secret)) {
            mfaConfigs[userId] = mfaConfig.copy(enabled = true)
            return true
        }
        return false
    }

    fun disableMfa(userId: String): Boolean {
        return mfaConfigs.remove(userId) != null
    }

    fun isMfaEnabled(userId: String): Boolean {
        val mfaConfig = mfaConfigs[userId]
        return mfaConfig?.enabled == true
    }

    fun getMfaSecret(userId: String): String? {
        return mfaConfigs[userId]?.secret
    }

    private fun generateSecret(): String {
        val random = SecureRandom()
        val bytes = ByteArray(16)
        random.nextBytes(bytes)
        return Base64.getEncoder().encodeToString(bytes)
            .replace("=", "")
            .take(16)
            .mapIndexed { index, char ->
                if (index > 0 && index % 4 == 0) char.lowercaseChar() else char.uppercaseChar()
            }
            .joinToString("")
    }
}
