package com.example.push

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

/** Creates a PendingIntent that opens a Conversation screen when a notification is tapped. */
object ConversationPendingIntent {
    fun create(context: Context, data: Map<String, String>): PendingIntent {
        val intent = Intent(context, ConversationActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            data.forEach { (k, v) -> putExtra(k, v) }
        }
        val flags = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            0
        }) or PendingIntent.FLAG_UPDATE_CURRENT
        return PendingIntent.getActivity(context, 0, intent, flags)
    }
}
