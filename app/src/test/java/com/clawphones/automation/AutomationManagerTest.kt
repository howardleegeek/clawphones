package com.clawphones.automation

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import androidx.work.PeriodicWorkRequest
import java.util.concurrent.TimeUnit

class AutomationManagerTest {
    @Test
    fun schedulesDailyBackupWith24HourInterval() {
        var capturedName: String? = null
        var capturedRequest: PeriodicWorkRequest? = null

        val fakeScheduler = object : Scheduler {
            override fun schedulePeriodicWork(name: String, request: PeriodicWorkRequest) {
                capturedName = name
                capturedRequest = request
            }
            override fun cancelPeriodicWork(name: String) {
                // no-op for this test
            }
        }

        val manager = AutomationManager(fakeScheduler)
        manager.scheduleDailyBackup(2) // delay 2 hours

        assertEquals("daily_backup", capturedName)
        assertNotNull(capturedRequest)
        val req = capturedRequest!!
        assertEquals(24L, req.intervalDuration)
        assertEquals(TimeUnit.HOURS, req.intervalUnit)
    }
}
