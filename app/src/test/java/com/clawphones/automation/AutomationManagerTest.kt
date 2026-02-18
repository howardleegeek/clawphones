package com.clawphones.automation

import org.junit.Assert.*
import org.junit.Test

class AutomationManagerTest {
    @Test
    fun testAutoReplyTriggeredOnMessageHello() {
        val taskId = "G8-02-CP"
        val manager = AutomationManager(taskId)

        var replyText: String? = null
        val replyAction = object : AutomationAction {
            override fun execute(event: UserEvent) {
                val text = event.payload["text"] ?: ""
                replyText = "Auto: $text"
            }
        }

        val rule = AutomationRule(
            eventType = EventType.MESSAGE_RECEIVED,
            condition = { it.payload["text"] == "hello" },
            action = replyAction
        )
        manager.registerRule(rule)

        manager.receiveEvent(UserEvent(EventType.MESSAGE_RECEIVED, mapOf("text" to "hello")))

        assertEquals("Auto: hello", replyText)
    }

    @Test
    fun testSceneSwitchTriggeredOnSceneHome() {
        val taskId = "G8-02-CP"
        val manager = AutomationManager(taskId)

        var switchedTo: String? = null
        val switchAction = object : AutomationAction {
            override fun execute(event: UserEvent) {
                switchedTo = event.payload["scene"]
            }
        }

        val rule = AutomationRule(
            eventType = EventType.SCENE_CHANGED,
            condition = { it.payload["scene"] == "home" },
            action = switchAction
        )
        manager.registerRule(rule)

        manager.receiveEvent(UserEvent(EventType.SCENE_CHANGED, mapOf("scene" to "home")))

        assertEquals("home", switchedTo)
    }
}
