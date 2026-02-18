package com.clawphones.data

import android.content.Context
import android.content.SharedPreferences
import com.clawphones.security.SecurityModule

class PreferencesManager(
    context: Context,
    private val taskId: String = "G12-05-CP"
) {
    companion object {
        private const val PREFS_NAME = "clawphones_prefs"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_USER_TOKEN = "user_token"
        private const val KEY_API_KEY = "api_key"
        private const val KEY_USER_NAME = "user_name"
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val securityModule = SecurityModule(taskId)

    fun saveAuthToken(token: String) {
        val encryptedToken = securityModule.encrypt(token)
        sharedPreferences.edit().putString(KEY_AUTH_TOKEN, encryptedToken).apply()
    }

    fun getAuthToken(): String? {
        val encryptedToken = sharedPreferences.getString(KEY_AUTH_TOKEN, null)
        return encryptedToken?.let { securityModule.decrypt(it) }
    }

    fun saveUserToken(token: String) {
        val encryptedToken = securityModule.encrypt(token)
        sharedPreferences.edit().putString(KEY_USER_TOKEN, encryptedToken).apply()
    }

    fun getUserToken(): String? {
        val encryptedToken = sharedPreferences.getString(KEY_USER_TOKEN, null)
        return encryptedToken?.let { securityModule.decrypt(it) }
    }

    fun saveApiKey(apiKey: String) {
        val encryptedKey = securityModule.encrypt(apiKey)
        sharedPreferences.edit().putString(KEY_API_KEY, encryptedKey).apply()
    }

    fun getApiKey(): String? {
        val encryptedKey = sharedPreferences.getString(KEY_API_KEY, null)
        return encryptedKey?.let { securityModule.decrypt(it) }
    }

    fun saveUserName(userName: String) {
        val encryptedName = securityModule.encrypt(userName)
        sharedPreferences.edit().putString(KEY_USER_NAME, encryptedName).apply()
    }

    fun getUserName(): String? {
        val encryptedName = sharedPreferences.getString(KEY_USER_NAME, null)
        return encryptedName?.let { securityModule.decrypt(it) }
    }

    fun saveSensitiveData(key: String, value: String) {
        val encryptedValue = securityModule.encrypt(value)
        sharedPreferences.edit().putString(key, encryptedValue).apply()
    }

    fun getSensitiveData(key: String): String? {
        val encryptedValue = sharedPreferences.getString(key, null)
        return encryptedValue?.let { securityModule.decrypt(it) }
    }

    fun removeSensitiveData(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }

    fun clearAllSensitiveData() {
        sharedPreferences.edit().clear().apply()
    }

    fun containsSensitiveData(key: String): Boolean {
        return sharedPreferences.contains(key)
    }
}
