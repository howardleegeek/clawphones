package com.clawphones.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.security.MessageDigest
import kotlin.text.Charsets

/**
 * Security utilities for data encryption/decryption using AES-GCM.
 * Key must be initialized before usage via setKeyFromString or initKey.
 */
object SecurityModule {
    private var keySpec: SecretKeySpec? = null

    fun initKey(keyBytes: ByteArray) {
        keySpec = SecretKeySpec(keyBytes, "AES")
    }

    // Convenience: derive a 128-bit AES key from a string (deterministic for tests)
    fun setKeyFromString(keyString: String) {
        val md = MessageDigest.getInstance("SHA-256")
        val key = md.digest(keyString.toByteArray(Charsets.UTF_8)).copyOf(16)
        initKey(key)
    }

    fun encrypt(plaintext: ByteArray): ByteArray {
        val key = keySpec ?: throw IllegalStateException("Encryption key not initialized")
        val iv = ByteArray(12)
        SecureRandom().nextBytes(iv)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.ENCRYPT_MODE, key, spec)
        val ct = cipher.doFinal(plaintext)
        return iv + ct
    }

    fun decrypt(input: ByteArray): ByteArray {
        val key = keySpec ?: throw IllegalStateException("Encryption key not initialized")
        if (input.size < 12) throw IllegalArgumentException("Invalid encrypted data")
        val iv = input.copyOfRange(0, 12)
        val ct = input.copyOfRange(12, input.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)
        return cipher.doFinal(ct)
    }

    fun encryptFlow(input: Flow<ByteArray>): Flow<ByteArray> {
        val key = keySpec ?: throw IllegalStateException("Encryption key not initialized")
        return input.map { chunk ->
            val iv = ByteArray(12)
            SecureRandom().nextBytes(iv)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.ENCRYPT_MODE, key, spec)
            val ct = cipher.doFinal(chunk)
            val res = ByteArray(iv.size + ct.size)
            System.arraycopy(iv, 0, res, 0, iv.size)
            System.arraycopy(ct, 0, res, iv.size, ct.size)
            res
        }
    }

    fun decryptFlow(input: Flow<ByteArray>): Flow<ByteArray> {
        val key = keySpec ?: throw IllegalStateException("Encryption key not initialized")
        return input.map { encChunk ->
            if (encChunk.size < 12) throw IllegalArgumentException("Invalid encrypted chunk")
            val iv = encChunk.copyOfRange(0, 12)
            val ct = encChunk.copyOfRange(12, encChunk.size)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, key, spec)
            cipher.doFinal(ct)
        }
    }
}
