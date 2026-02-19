package com.clawphones.automation

import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import androidx.work.PeriodicWorkRequest
import androidx.work.OneTimeWorkRequest

class AutomationModuleTest {
    @Test
    fun testBuildWorkRequests_createsCorrectTypes() {
        val configs = listOf(
            AutomationTaskConfig(id = "t1", taskType = "SYNC", intervalHours = 24),
            AutomationTaskConfig(id = "t2", taskType = "BACKUP", intervalHours = 0)
        )

        val requests = AutomationModule.buildWorkRequests(configs)
        assertEquals(2, requests.size)
        assertTrue(requests[0] is PeriodicWorkRequest)
        assertTrue(requests[1] is OneTimeWorkRequest)
    }
}
