package com.clawphones.ui.main

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Button
import android.view.View
import android.view.Gravity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity

// Minimal main activity that shows login status and a login button when not logged in.
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            val padding = dpToPx(16)
            setPadding(padding, padding, padding, padding)
        }

        // UI element showing current login status
        val statusView = TextView(this).apply {
            textSize = 18f
            text = loginDisplayText(false) // default to not logged in
        }

        // Button to trigger login action (simplified for this example)
        val loginButton = Button(this).apply {
            text = "登录"
            setOnClickListener {
                // Simulate a login action by updating the status text
                statusView.text = loginDisplayText(true)
                this.visibility = View.GONE
            }
        }

        root.addView(statusView)
        root.addView(loginButton)
        setContentView(root)
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }
}

// Helper: return display text for given login state. Exposed to tests as a top-level function.
fun loginDisplayText(isLoggedIn: Boolean): String = if (isLoggedIn) "已登录" else "登录"
