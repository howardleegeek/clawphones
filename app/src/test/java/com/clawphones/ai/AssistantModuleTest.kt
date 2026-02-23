package com.clawphones.ai

import org.junit.Assert.assertEquals
import org.junit.Test

class AssistantModuleTest {

    @Test
    fun testProcessInputReturnsAIResponse() {
        val provider = object : AIProvider {
            override fun generateResponse(input: String): String {
                return when {
                    input.contains("hello", ignoreCase = true) -> "Hello there!"
                    input.contains("weather", ignoreCase = true) -> "Today's weather is sunny."
                    else -> "I don't know."
                }
            }
        }

        val module = AssistantModule(provider, "test-task-001")
        val result = module.processInput("hello")
        assertEquals("Hello there!", result)
    }

    @Test
    fun testProcessInputHandlesException() {
        val provider = object : AIProvider {
            override fun generateResponse(input: String): String {
                throw RuntimeException("AI failure")
            }
        }

        val module = AssistantModule(provider, "test-task-002")
        val result = module.processInput("anything")
        assertEquals("Sorry, something went wrong.", result)
    }
}
