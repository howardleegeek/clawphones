package com.clawphones.app.analytics

// Lightweight AI-assisted analytics manager with in-memory data collection
// and a tiny heuristic AI analyzer. Designed for testability in this task.
data class UserEvent(val name: String, val properties: Map<String, String>, val timestamp: Long)
data class AIResult(val recommendedItems: List<String>)

class AnalyticsManager(val taskId: String? = null) {
    private val events = mutableListOf<UserEvent>()
    private val aiEngine = AIAnalyzer()

    fun logEvent(eventName: String, properties: Map<String, String> = emptyMap()) {
        val evt = UserEvent(eventName, properties, System.currentTimeMillis())
        events.add(evt)
        logState("logEvent", eventName, evt)
    }

    fun collectData(): List<UserEvent> = events.toList()

    fun getLastEvent(): UserEvent? = events.lastOrNull()

    fun clear() { events.clear() }

    fun getRecommendations(): List<String> = aiEngine.analyze(events).recommendedItems

    private fun logState(action: String, detail: String, evt: UserEvent) {
        // Contextual log for debugging/triage
        android.util.Log.d("AnalyticsManager", "taskId=${taskId ?: "N/A"} action=$action detail=$detail events=${events.size}")
    }

    // Tiny, internal AI-like analysis
    private inner class AIAnalyzer {
        fun analyze(list: List<UserEvent>): AIResult {
            val counts = list.groupingBy { it.name }.eachCount()
            val recs = mutableListOf<String>()
            if ((counts["login"] ?: 0) > 0) recs.add("Improve onboarding experience")
            if ((counts["browse_item"] ?: 0) > 0 || (counts["view_recommendation"] ?: 0) > 0)
                recs.add("Enhance personalized recommendations")
            if ((counts["add_to_cart"] ?: 0) > 0) recs.add("Reduce cart abandonment with timely prompts")
            if (recs.isEmpty()) recs.add("Collect more diverse interaction signals")
            return AIResult(recommendedItems = recs)
        }
    }
}
