package com.clawphones.security

import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom
import java.util.Base64

/**
 * AES helper that derives a 128-bit key from a seed string and provides
 * simple encrypt/decrypt using AES/CBC/PKCS5Padding.
 */
class AesUtil(seed: String) {
    private val secretKeySpec: SecretKeySpec

    init {
        val sha = MessageDigest.getInstance("SHA-256")
        val keyBytes = sha.digest(seed.toByteArray(Charsets.UTF_8)).copyOf(16)
        secretKeySpec = SecretKeySpec(keyBytes, "AES")
    }

    /** Encrypts the plaintext bytes and returns a Base64 string containing IV + ciphertext. */
    fun encrypt(plaintext: ByteArray): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val iv = ByteArray(16)
        SecureRandom().nextBytes(iv)
        val ivSpec = IvParameterSpec(iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec)
        val cipherText = cipher.doFinal(plaintext)
        val combined = ByteArray(iv.size + cipherText.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(cipherText, 0, combined, iv.size, cipherText.size)
        return Base64.getEncoder().encodeToString(combined)
    }

    /** Decrypts the payload produced by encrypt(). */
    fun decrypt(payload: String): ByteArray {
        val combined = Base64.getDecoder().decode(payload)
        val iv = combined.copyOfRange(0, 16)
        val cipherText = combined.copyOfRange(16, combined.size)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val ivSpec = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec)
        return cipher.doFinal(cipherText)
    }
}
