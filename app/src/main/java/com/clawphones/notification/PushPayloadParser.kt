package com.clawphones.notification

object PushPayloadParser {
    /**
     * Parses a data payload from FCM into a PushPayload.
     * Returns null if there is no recognizable content to display as a notification.
     */
    fun parseFromData(data: Map<String, String>): PushPayload? {
        if (data.isEmpty()) return null

        val title = data["title"]
        val body = data["body"] ?: data["message"]
        val conversationId = data["conversation_id"] ?: data["chat_id"]
        val silent = data["silent"]?.toBoolean() ?: false

        // Require at least a title or a body to show a notification
        if (title.isNullOrBlank() && body.isNullOrBlank()) {
            // Might be a pure silent push intended for sync
            // If silent flag is true, still return payload with silent = true
            if (silent) {
                return PushPayload(title, body, conversationId, true)
            }
            return null
        }

        return PushPayload(title, body, conversationId, silent)
    }
}
