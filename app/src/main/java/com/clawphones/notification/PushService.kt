package com.clawphones.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/** Android service to receive FCM messages and trigger notifications or sync. */
class PushService : FirebaseMessagingService() {
    companion object {
        private const val CHANNEL_ID = "clawphones_push_channel"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val data = remoteMessage.data ?: emptyMap()
        val payload = PushPayloadParser.parseFromData(data) ?: return

        if (payload.silent) {
            scheduleBackgroundSync(payload)
        } else {
            showNotification(this, payload)
        }
    }

    override fun onNewToken(token: String) {
        // NOOP for this mock; in real app, send token to backend
    }

    private fun scheduleBackgroundSync(payload: PushPayload) {
        // Placeholder for background sync logic (e.g., WorkManager)
        // For tests, this can be observed via method calls if extended.
    }

    private fun showNotification(context: Context, payload: PushPayload) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Push Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val title = payload.title ?: "New message"
        val body = payload.body ?: ""

        // Click action: open conversation if conversationId is present
        val clickIntent = if (payload.conversationId != null) {
            // Use a generic action to be handled by the app's receiver
            Intent("com.clawphones.OPEN_CONVERSATION").apply {
                putExtra("conversation_id", payload.conversationId)
            }
        } else {
            Intent()
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            clickIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_UPDATE_CURRENT else 0
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationId = payload.conversationId?.hashCode() ?: 0
        notificationManager.notify(notificationId, notification)
    }
}
