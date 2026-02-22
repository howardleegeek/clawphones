package com.example.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

/** Helper to show in-app notifications for FCM messages. */
object NotificationHelper {
    private const val CHANNEL_ID = "clawphones_messages"

    fun showNotification(context: Context, title: String, body: String, data: Map<String, String>) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Messages", NotificationManager.IMPORTANCE_DEFAULT)
            nm.createNotificationChannel(channel)
        }
        val pendingIntent = ConversationPendingIntent.create(context, data)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        nm.notify(1, notification)
        // The click intent to open a conversation is intentionally omitted in this
        // minimal implementation to keep unit testability straightforward.
    }
}
