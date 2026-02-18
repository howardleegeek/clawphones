package com.clawphones.ui.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import android.util.Log
import kotlinx.coroutines.launch

// Destination options after startup check
enum class StartupDestination { MAIN, LOGIN }

// Simple token storage using SharedPreferences
class TokenStore(private val context: Context) {
    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    fun getToken(): String? = prefs.getString("auth_token", null)
    fun setToken(token: String) = prefs.edit().putString("auth_token", token).apply()
}

// Token validation contract
interface ITokenValidator {
    fun isTokenValid(token: String): Boolean
}

// Default, lightweight token validator
class DefaultTokenValidator: ITokenValidator {
    override fun isTokenValid(token: String): Boolean {
        if (token.isBlank()) return false
        if (token == "expired") return false
        return true
    }
}

// Orchestrates startup decision based on stored token
class LoginStateManager(
    private val tokenProvider: suspend () -> String?,
    private val validator: ITokenValidator = DefaultTokenValidator()
) {
    suspend fun startupDestination(): StartupDestination {
        val token = tokenProvider()
        return if (token != null && validator.isTokenValid(token)) StartupDestination.MAIN else StartupDestination.LOGIN
    }
}

// Main activity that triggers the startup-destination check
class MainActivity : AppCompatActivity() {
    private val logTag = "G12-03-CP/MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // In this minimal example we avoid inflating a layout to keep logic isolated for tests
        lifecycleScope.launch {
            val tokenStore = TokenStore(this@MainActivity)
            val destination = LoginStateManager({ tokenStore.getToken() }).startupDestination()
            Log.i(logTag, "Determined startup destination: $destination [task_id=G12-03-CP]")
            navigate(destination)
        }
    }

    private fun navigate(destination: StartupDestination) {
        val intent = when (destination) {
            StartupDestination.MAIN -> Intent(this, MainScreenActivity::class.java)
            StartupDestination.LOGIN -> Intent(this, LoginActivity::class.java)
        }
        startActivity(intent)
        finish()
    }
}

// Placeholder activities to satisfy possible compilation in isolated environments
class MainScreenActivity : Activity()
class LoginActivity : Activity()
