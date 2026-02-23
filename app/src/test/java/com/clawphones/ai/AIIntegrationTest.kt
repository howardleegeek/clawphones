package com.clawphones.ai

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AIIntegrationTest {
    @Test
    fun testModelLoadsAndPredicts() {
        // Ensure model loads and returns a deterministic prediction
        assertTrue(AIIntegration.loadModel())
        val suggestion = AIIntegration.getSuggestion("Please complete the task")
        assertEquals("Suggested: complete task in 1 hour", suggestion)
    }

    @Test
    fun testAutomation() {
        AIIntegration.loadModel()
        val action = AIIntegration.automate("schedule a meeting")
        assertEquals("AUTO-Suggested: schedule meeting", action)
    }
}
