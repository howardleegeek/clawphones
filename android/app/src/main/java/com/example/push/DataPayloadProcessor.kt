package com.example.push

/** Helpers to interpret FCM data payloads in a testable way. */
class DataPayloadProcessor {
    companion object {
        fun isSilentPush(data: Map<String, String>): Boolean {
            return data["silent"]?.toBoolean() ?: false
        }

        fun getConversationId(data: Map<String, String>): String? {
            return data["conversation_id"]
        }
    }
}
