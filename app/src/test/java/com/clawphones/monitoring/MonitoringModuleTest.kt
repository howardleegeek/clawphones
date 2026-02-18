package com.clawphones.monitoring

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests for MonitoringModule crash reporting integration.
 */
class MonitoringModuleTest {
    private class FakeCrashlyticsService : CrashlyticsService {
        data class Log(val e: Throwable, val message: String?, val taskId: String?)
        val logs = mutableListOf<Log>()

        override fun logError(e: Throwable, message: String?, taskId: String?) {
            logs.add(Log(e, message, taskId))
        }

        override fun logInfo(message: String, taskId: String?) {
            // no-op for tests
        }
    }

    @Test
    fun testInitializationAndCrashLoggingIncludesContext() {
        val fake = FakeCrashlyticsService()
        // Initialize with fake service and a task context
        MonitoringModule.initialize(service = fake, taskId = "G13-06-CP")

        val ex = RuntimeException("boom")
        MonitoringModule.reportCrash(ex, "test crash")

        // Verify that crash was logged with correct context and message
        assertEquals(1, fake.logs.size)
        val log = fake.logs[0]
        assertEquals(ex, log.e)
        assertEquals("test crash", log.message)
        assertEquals("G13-06-CP", log.taskId)
    }
}
