package com.clawphones.innovation

import java.util.concurrent.atomic.AtomicLong

/** Simple in-memory innovation module for recommendations.
 *  This implementation is lightweight and designed for tests
 *  and demonstration purposes.
 */
class InnovationModule(private val taskId: String? = null) {
    private val logPrefix = taskId?.let { "[task_id=$it]" } ?: ""
    private fun log(msg: String) {
        if (logPrefix.isNotEmpty()) println("$logPrefix $msg") else println(msg)
    }

    data class ContentItem(val id: String, val title: String, val category: String)

    enum class UserActionType { VIEW, LIKE, SHARE }

    data class UserAction(
        val userId: String,
        val contentId: String,
        val actionType: UserActionType,
        val timestamp: Long
    )

    // Simple catalog of content items available for recommendation
    private val catalog = listOf(
        ContentItem("content_1", "Kotlin for Beginners", "Education"),
        ContentItem("content_2", "Advanced Kotlin Flows", "Education"),
        ContentItem("content_3", "Android App Architecture", "Technology"),
        ContentItem("content_4", "UI/UX Design Basics", "Design"),
        ContentItem("content_5", "Machine Learning for Recommendations", "Technology")
    )

    // In-memory log of user actions (acts as a very lightweight training dataset)
    private val actionLog = mutableListOf<UserAction>()

    // Seed some data for demonstration and tests
    init {
        seedData()
    }

    private fun seedData() {
        val now = System.currentTimeMillis()
        actionLog.add(UserAction("user_01", "content_1", UserActionType.VIEW, now - 10000))
        actionLog.add(UserAction("user_01", "content_2", UserActionType.LIKE, now - 20000))
        actionLog.add(UserAction("user_02", "content_3", UserActionType.VIEW, now - 30000))
    }

    /** Record a user action for later use in recommendations. */
    fun recordUserAction(userId: String, contentId: String, actionType: UserActionType) {
        val a = UserAction(userId, contentId, actionType, System.currentTimeMillis())
        actionLog.add(a)
        log("Recorded action: user=$userId, content=$contentId, type=$actionType")
    }

    /** Return a list of recommended content items for a given user.
     *  The scoring is lightweight: items similar to past user actions (same category)
     *  and exact matches on content ids increase the score. Items already acted upon by
     *  the user are filtered out.
     */
    fun getRecommendedContent(userId: String, limit: Int = 5): List<ContentItem> {
        val acted = actionLog.filter { it.userId == userId }.map { it.contentId }.toSet()

        // compute a simple score for each item
        fun scoreFor(item: ContentItem): Int {
            var score = 0
            val userActions = actionLog.filter { it.userId == userId }
            for (act in userActions) {
                // If the action targets the same content, boost score
                if (act.contentId == item.id) {
                    score += when (act.actionType) {
                        UserActionType.VIEW -> 2
                        UserActionType.LIKE -> 5
                        UserActionType.SHARE -> 3
                    }
                }
                // If the action shares the same category, small boost
                val actedItem = catalog.find { it.id == act.contentId }
                if (actedItem != null && actedItem.category == item.category) {
                    score += 2
                }
            }
            return score
        }

        return catalog
            .filter { it.id !in acted }
            .map { it to scoreFor(it) }
            .sortedByDescending { it.second }
            .take(limit)
            .map { it.first }
    }
}
