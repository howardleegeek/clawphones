package com.example.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Handles incoming FCM messages. Distinguishes between silent data pushes (for
 * background sync) and visible notifications.
 */
class FCMService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data ?: emptyMap()
        val isSilent = data["silent"]?.toBoolean() ?: false
        if (isSilent) {
            // Trigger background synchronization for silent pushes
            val conversationId = data["conversation_id"]
            BackgroundSync.scheduleConversationSync(conversationId)
        } else {
            // Show a user-facing notification for regular pushes
            val title = message.notification?.title ?: data["title"] ?: "New Message"
            val body = message.notification?.body ?: data["body"] ?: "You have a new message"
            NotificationHelper.showNotification(this, title, body, data)
        }
    }

    override fun onNewToken(token: String) {
        // In production, the token should be sent to the server. Omitted here for brevity.
        super.onNewToken(token)
    }
}
