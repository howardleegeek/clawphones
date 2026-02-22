package com.example.push

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/** Simple placeholder Activity that would display a conversation. */
class ConversationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // No layout needed for the minimal testable implementation.
        // Extras from the notification payload can be read here if needed.
        val extras = intent.extras
        // No-op: this is a stub to enable navigation from notifications in tests.
        ( extras?.size() ?: 0 )
    }
}
