package com.example.push

/**
 * Lightweight placeholder for background sync triggered by silent pushes.
 * In production, this would enqueue a WorkManager job or similar.
 */
object BackgroundSync {
    var lastScheduledConversationId: String? = null

    fun scheduleConversationSync(conversationId: String?) {
        // Record the last conversation id for testability; actual work is out of scope here.
        lastScheduledConversationId = conversationId
    }
}
