package com.clawphones.ai

/**
 * AI provider abstraction. Implementations can call a real AI service or a mock for tests.
 */
interface AIProvider {
    fun generateResponse(input: String): String
}

/**
 * Core assistant module that processes user input and returns AI-generated responses.
 * Logs are emitted with the taskId to provide traceability in logs.
 */
class AssistantModule(private val aiProvider: AIProvider, private val taskId: String) {

    /** Process raw user input and obtain a textual response from the AI provider. */
    fun processInput(input: String): String {
        log("Processing input: \"$input\"")
        return try {
            val response = aiProvider.generateResponse(input)
            log("Generated response: \"$response\"")
            response
        } catch (e: Exception) {
            log("Error processing input: ${e.message}")
            // Fallback to a safe default message on error
            "Sorry, something went wrong."
        }
    }

    /** Lightweight logger that includes the task_id context in every log line. */
    private fun log(message: String) {
        println("[task_id=$taskId] $message")
    }
}
