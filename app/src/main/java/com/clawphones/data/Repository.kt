package com.clawphones.data

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class Repository private constructor() {

    private var encryptionKey: ByteArray? = null

    companion object {
        private const val ALGORITHM = "AES/GCM/NoPadding"
        private const val KEY_LENGTH = 256
        private const val GCM_TAG_LENGTH = 128
        private const val GCM_IV_LENGTH = 12
        private const val PBKDF2_ITERATIONS = 65536
        private const val SALT_LENGTH = 16

        @Volatile
        private var instance: Repository? = null

        fun getInstance(): Repository {
            return instance ?: synchronized(this) {
                instance ?: Repository().also { instance = it }
            }
        }
    }

    fun initialize(password: String, salt: ByteArray) {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH)
        val tmp = factory.generateSecret(spec)
        encryptionKey = tmp.encoded
        spec.clearPassword()
    }

    fun initializeWithKey(key: ByteArray) {
        encryptionKey = key.copyOf()
    }

    private fun getKey(): ByteArray {
        return encryptionKey ?: throw IllegalStateException("Repository not initialized. Call initialize() first.")
    }

    fun encrypt(plaintext: ByteArray): ByteArray {
        val key = getKey()
        val cipher = Cipher.getInstance(ALGORITHM)

        val iv = ByteArray(GCM_IV_LENGTH)
        SecureRandom().nextBytes(iv)

        val keySpec = SecretKeySpec(key, "AES")
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)

        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec)
        val ciphertext = cipher.doFinal(plaintext)

        val result = ByteArray(iv.size + ciphertext.size)
        System.arraycopy(iv, 0, result, 0, iv.size)
        System.arraycopy(ciphertext, 0, result, iv.size, ciphertext.size)

        return result
    }

    fun encrypt(plaintext: String): String {
        val encrypted = encrypt(plaintext.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(encrypted, Base64.NO_WRAP)
    }

    fun decrypt(encryptedData: ByteArray): ByteArray {
        val key = getKey()

        val iv = encryptedData.copyOfRange(0, GCM_IV_LENGTH)
        val ciphertext = encryptedData.copyOfRange(GCM_IV_LENGTH, encryptedData.size)

        val cipher = Cipher.getInstance(ALGORITHM)
        val keySpec = SecretKeySpec(key, "AES")
        val gcmSpec = GCMParameterSpec(GCM_TAG_LENGTH, iv)

        cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec)
        return cipher.doFinal(ciphertext)
    }

    fun decrypt(encryptedBase64: String): String {
        val encryptedData = Base64.decode(encryptedBase64, Base64.NO_WRAP)
        val decrypted = decrypt(encryptedData)
        return String(decrypted, Charsets.UTF_8)
    }

    fun save(key: String, value: String) {
        val encryptedValue = encrypt(value)
        InMemoryStorage.put(key, encryptedValue)
    }

    fun load(key: String): String? {
        val encryptedValue = InMemoryStorage.get(key) ?: return null
        return decrypt(encryptedValue)
    }

    fun clear() {
        encryptionKey?.fill(0)
        encryptionKey = null
    }

    object InMemoryStorage {
        private val storage = mutableMapOf<String, String>()

        fun put(key: String, value: String) {
            storage[key] = value
        }

        fun get(key: String): String? {
            return storage[key]
        }

        fun remove(key: String) {
            storage.remove(key)
        }

        fun clear() {
            storage.clear()
        }
    }
}
