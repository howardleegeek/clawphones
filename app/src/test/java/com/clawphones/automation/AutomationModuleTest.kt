package com.clawphones.automation

import org.junit.Assert.assertEquals
import org.junit.Test

class AutomationModuleTest {
    @Test
    fun testBuildAutomationWorkRequestContainsTaskData() {
        // Given a simple automation task config
        val config = AutomationTaskConfig(id = "task-sync", taskType = "DataSync", intervalHours = 24L)

        // When we build work requests from the config
        val requests = AutomationModule.buildWorkRequests(listOf(config))

        // Then the produced WorkRequest should carry the task data
        val data = requests[0].inputData
        assertEquals("task-sync", data.getString("TASK_ID"))
        assertEquals("DataSync", data.getString("TASK_TYPE"))
    }
}
