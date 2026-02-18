package com.clawphones.ui.main

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.clawphones.R

class MainActivity : AppCompatActivity() {

    private lateinit var userNameText: TextView
    private lateinit var logoutButton: Button
    private lateinit var loginButton: Button
    private var isLoggedIn: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        userNameText = findViewById(R.id.user_name_text)
        logoutButton = findViewById(R.id.logout_button)
        loginButton = findViewById(R.id.login_button)

        updateUI()

        logoutButton.setOnClickListener {
            performLogout()
        }

        loginButton.setOnClickListener {
            performLogin()
        }
    }

    private fun updateUI() {
        if (isLoggedIn) {
            userNameText.text = getString(R.string.welcome_user)
            userNameText.visibility = TextView.VISIBLE
            logoutButton.visibility = Button.VISIBLE
            loginButton.visibility = Button.GONE
        } else {
            userNameText.visibility = TextView.GONE
            logoutButton.visibility = Button.GONE
            loginButton.visibility = Button.VISIBLE
        }
    }

    fun performLogout() {
        isLoggedIn = false
        clearUserSession()
        updateUI()
    }

    fun performLogin() {
        isLoggedIn = true
        updateUI()
    }

    private fun clearUserSession() {
        val sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()
    }

    fun isUserLoggedIn(): Boolean = isLoggedIn
}
