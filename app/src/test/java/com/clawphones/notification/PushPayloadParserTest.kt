package com.clawphones.notification

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PushPayloadParserTest {
    @Test
    fun parsesDataPayloadWithAllFields() {
        val data = mapOf(
            "title" to "Hi",
            "body" to "There",
            "conversation_id" to "conv-42",
            "silent" to "false"
        )
        val payload = PushPayloadParser.parseFromData(data)
        assertTrue(payload != null)
        assertEquals("Hi", payload?.title)
        assertEquals("There", payload?.body)
        assertEquals("conv-42", payload?.conversationId)
        assertEquals(false, payload?.silent)
    }

    @Test
    fun silentPushParsesWithoutTitleBody() {
        val data = mapOf("conversation_id" to "conv-99", "silent" to "true")
        val payload = PushPayloadParser.parseFromData(data)
        assertTrue(payload != null)
        assertEquals(null, payload?.title)
        assertEquals(null, payload?.body)
        assertEquals("conv-99", payload?.conversationId)
        assertEquals(true, payload?.silent)
    }

    @Test
    fun returnsNullWhenNoContentAndNotSilent() {
        val data = mapOf("conversation_id" to "conv-1")
        val payload = PushPayloadParser.parseFromData(data)
        assertNull(payload)
    }
}
