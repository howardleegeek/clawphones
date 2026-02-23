package com.clawphones.app.security

import android.content.Context
import android.util.Base64
import android.util.Log
import java.nio.charset.Charset
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties

/**
 * Crypto manager using Android Keystore for AES-GCM encryption.
 * Provides simple encrypt/decrypt for sensitive strings.
 * NOTE: This is a lightweight in-app encryption helper. For full DB encryption,
 * consider integrating an encrypted database layer (e.g., SQLCipher) in the
 * data layer. This class keeps public surface stable and adds minimal footprint.
 */
data class EncryptedData(val iv: String, val ciphertext: String)

object CryptoManager {
    private const val TAG = "CryptoManager"
    private const val KEY_ALIAS = "clawphones_master_key"
    private const val AES_MODE = "AES/GCM/NoPadding"
    private const val TASK_ID = "G11-04-CP"

    @Volatile
    private var secretKey: SecretKey? = null

    /** Initialize key material. Call once before first use. */
    fun init(context: Context) {
        secretKey = getOrCreateSecretKey()
        Log.d(TAG, "[$TASK_ID] CryptoManager initialized")
    }

    /** Encrypt plaintext using AES-GCM. */
    fun encrypt(plaintext: String): EncryptedData {
        val key = secretKey ?: getOrCreateSecretKey().also { secretKey = it }
        val cipher = Cipher.getInstance(AES_MODE)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        val plaintextBytes = plaintext.toByteArray(Charset.forName("UTF-8"))
        val ciphertext = cipher.doFinal(plaintextBytes)
        val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)
        val ctBase64 = Base64.encodeToString(ciphertext, Base64.NO_WRAP)
        Log.d(TAG, "[$TASK_ID] data encrypted")
        return EncryptedData(ivBase64, ctBase64)
    }

    /** Decrypt data produced by encrypt(). */
    fun decrypt(data: EncryptedData): String {
        val key = secretKey ?: getOrCreateSecretKey().also { secretKey = it }
        val iv = Base64.decode(data.iv, Base64.NO_WRAP)
        val ct = Base64.decode(data.ciphertext, Base64.NO_WRAP)
        val cipher = Cipher.getInstance(AES_MODE)
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)
        val plaintextBytes = cipher.doFinal(ct)
        val plaintext = String(plaintextBytes, Charset.forName("UTF-8"))
        Log.d(TAG, "[$TASK_ID] data decrypted")
        return plaintext
    }

    /** Retrieve or create a master AES key in Android Keystore. */
    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val existing = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        if (existing != null) {
            return existing.secretKey
        }
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        val keySpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        keyGenerator.init(keySpec)
        return keyGenerator.generateKey()
    }
}
