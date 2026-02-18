package com.clawphones.automation

// Simple event-driven automation manager
enum class EventType { MESSAGE_RECEIVED, SCENE_CHANGED, BUTTON_CLICK }

data class UserEvent(val type: EventType, val payload: Map<String, String> = emptyMap())

interface AutomationAction {
    fun execute(event: UserEvent)
}

data class AutomationRule(
    val eventType: EventType,
    val condition: (UserEvent) -> Boolean,
    val action: AutomationAction
)

class AutomationManager(private val taskId: String) {
    private val rules = mutableListOf<AutomationRule>()

    // Lightweight logger that includes task_id for traceability
    private fun log(message: String) {
        println("task_id=$taskId | $message")
    }

    fun registerRule(rule: AutomationRule) {
        rules.add(rule)
        log("Registered rule for ${rule.eventType}")
    }

    fun receiveEvent(event: UserEvent) {
        log("Received event ${event.type}")
        for (rule in rules) {
            if (rule.eventType == event.type && rule.condition(event)) {
                log("Triggering action for event ${event.type}")
                rule.action.execute(event)
            }
        }
    }
}
