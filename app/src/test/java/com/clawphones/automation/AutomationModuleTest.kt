package com.clawphones.automation

import org.junit.Assert.assertEquals
import org.junit.Test

class AutomationModuleTest {
    @Test
    fun testBuildAutomationWorkRequestContainsTaskId() {
        val task = AutomationTask(id = "task-sync", name = "Data Sync", intervalHours = 24L)
        val request = AutomationModule.buildAutomationWorkRequest(task)
        val idFromData = request.inputData.getString(AutomationModule.INPUT_TASK_ID)
        assertEquals("task-sync", idFromData)
    }
}
