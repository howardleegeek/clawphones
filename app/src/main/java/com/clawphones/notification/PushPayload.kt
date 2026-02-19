package com.clawphones.notification

data class PushPayload(
    val title: String?,
    val body: String?,
    val conversationId: String?,
    val silent: Boolean
)
