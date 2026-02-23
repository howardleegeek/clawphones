package com.clawphones.ai

/**
 * Lightweight AI integration placeholder.
 * This implementation simulates loading a model and running simple inferences.
 * It is designed for unit testing in the kata while keeping behavior stable.
 */
object AIIntegration {
    private const val TASK_ID = "G9-05-CP"
    private var model: AIModel? = null

    interface AIModel {
        fun predict(input: String): String
    }

    private class SimpleAIModel : AIModel {
        override fun predict(input: String): String {
            val inpt = input.lowercase()
            return when {
                "task" in inpt -> "Suggested: complete task in 1 hour"
                "meeting" in inpt -> "Suggested: schedule meeting"
                else -> "Suggested: optimize workflow"
            }
        }
    }

    /** Load the AI model (simulated). */
    fun loadModel(): Boolean {
        log("Loading AI model...")
        model = SimpleAIModel()
        log("Model loaded.")
        return true
    }

    /** Get a smart suggestion based on input. */
    @Throws(IllegalStateException::class)
    fun getSuggestion(input: String): String {
        val m = model ?: throw IllegalStateException("Model not loaded")
        val res = m.predict(input)
        log("Prediction for input='$input': $res")
        return res
    }

    /** Perform an automated action based on the suggestion. */
    @Throws(IllegalStateException::class)
    fun automate(input: String): String {
        val m = model ?: throw IllegalStateException("Model not loaded")
        val pred = m.predict(input)
        val action = "AUTO-$pred"
        log("Automation action: $action")
        return action
    }

    private fun log(message: String) {
        println("AIIntegration[$TASK_ID] $message")
    }
}
