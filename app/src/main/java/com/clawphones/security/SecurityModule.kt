package com.clawphones.security

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Security module providing encryption/decryption and MFA utilities.
 * Note: This is a simplified implementation intended for tests and demonstration.
 */
class SecurityModule {
    companion object {
        private const val AES_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val AES_ALGO = "AES"
        private const val IV_SIZE = 12 // 96 bits for GCM
        private const val TAG_SIZE_BITS = 128

        // Deterministic derivation for testability. In production, use a proper KDF with salt/pepper.
        fun deriveKeyFromPassword(password: String): ByteArray {
            val salt = "salt-for-ptest".toByteArray()
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val spec = PBEKeySpec(password.toCharArray(), salt, 100000, 256)
            return factory.generateSecret(spec).encoded
        }
    }

    private val masterKey: ByteArray

    constructor(masterKey: ByteArray) {
        this.masterKey = masterKey
    }

    /** Convenience constructor to derive key from a password. */
    constructor(password: String) {
        this.masterKey = deriveKeyFromPassword(password)
    }

    /** Encrypts plaintext using AES-256-GCM. Returns IV and ciphertext. */
    fun encrypt(plaintext: ByteArray): EncryptedData {
        try {
            val iv = ByteArray(IV_SIZE)
            SecureRandom().nextBytes(iv)
            val keySpec = SecretKeySpec(masterKey, AES_ALGO)
            val cipher = Cipher.getInstance(AES_TRANSFORMATION)
            val spec = GCMParameterSpec(TAG_SIZE_BITS, iv)
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, spec)
            val cipherText = cipher.doFinal(plaintext)
            return EncryptedData(iv, cipherText)
        } catch (e: java.security.GeneralSecurityException) {
            throw java.lang.RuntimeException("Encryption failed: ${e.message}", e)
        }
    }

    /** Decrypts data produced by [encrypt]. */
    fun decrypt(data: EncryptedData): ByteArray {
        try {
            val keySpec = SecretKeySpec(masterKey, AES_ALGO)
            val cipher = Cipher.getInstance(AES_TRANSFORMATION)
            val spec = GCMParameterSpec(TAG_SIZE_BITS, data.iv)
            cipher.init(Cipher.DECRYPT_MODE, keySpec, spec)
            return cipher.doFinal(data.cipherText)
        } catch (e: java.security.GeneralSecurityException) {
            throw java.lang.RuntimeException("Decryption failed: ${e.message}", e)
        }
    }
}

data class EncryptedData(val iv: ByteArray, val cipherText: ByteArray)

/** Minimal MFA service providing TOTP-like codes based on a shared secret. */
class MfaService(private val sharedSecret: ByteArray) {
    fun generateTotp(timestampSeconds: Long = System.currentTimeMillis() / 1000L): String {
        val timestep = 30L
        val movingFactor = (timestampSeconds / timestep)
        val movingFactorBytes = java.nio.ByteBuffer.allocate(8).putLong(movingFactor).array()

        val mac = javax.crypto.Mac.getInstance("HmacSHA1")
        val keySpec = javax.crypto.spec.SecretKeySpec(sharedSecret, "RAW")
        mac.init(keySpec)
        val hash = mac.doFinal(movingFactorBytes)

        val offset = hash[hash.size - 1].toInt() and 0x0F
        val binary = ((hash[offset].toInt() and 0x7f) shl 24) or
                ((hash[offset + 1].toInt() and 0xff) shl 16) or
                ((hash[offset + 2].toInt() and 0xff) shl 8) or
                (hash[offset + 3].toInt() and 0xff)
        val otp = binary % 1_000_000
        return String.format("%06d", otp)
    }

    /** Verifies a provided code within a +/- window of time steps. */
    fun verifyTotp(code: String, timestampSeconds: Long = System.currentTimeMillis() / 1000L, window: Int = 1): Boolean {
        for (i in -window..window) {
            val t = timestampSeconds + i * 30L
            if (generateTotp(t) == code) return true
        }
        return false
    }
}
