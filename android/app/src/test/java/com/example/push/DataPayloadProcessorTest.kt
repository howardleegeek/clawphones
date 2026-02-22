package com.example.push

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DataPayloadProcessorTest {
    @Test
    fun silentPushDetection() {
        val silent = mapOf("silent" to "true")
        assertTrue(DataPayloadProcessor.isSilentPush(silent))
    }

    @Test
    fun nonSilentPushDetection() {
        val data = mapOf("silent" to "false")
        assertFalse(DataPayloadProcessor.isSilentPush(data))
    }

    @Test
    fun conversationIdExtraction() {
        val data = mapOf("conversation_id" to "conv-42")
        assertEquals("conv-42", DataPayloadProcessor.getConversationId(data))
    }
}
