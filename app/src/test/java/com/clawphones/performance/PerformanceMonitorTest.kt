package com.clawphones.performance

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PerformanceMonitorTest {

    class FakeTimeSource(private var current: Long, private val delta: Long) : PerformanceMonitor.TimeSource {
        override fun now(): Long {
            val v = current
            current += delta
            return v
        }
    }

    @Test
    fun testSectionTimingAccumulation() {
        val time = FakeTimeSource(0L, 1000L)
        val logs = mutableListOf<String>()
        val pm = PerformanceMonitor(taskId = "T1", log = { logs.add(it) }, timeSource = time)

        pm.startSection("load")
        pm.endSection("load")

        val report = pm.report()
        // 1 interval of 1000ns
        assertEquals(1000L, report.totalNanos)
        assertTrue(report.details.containsKey("load"))
    }

    @Test
    fun testAnalyzeBottlenecksDetectsMajor() {
        val time = FakeTimeSource(0L, 2_000_000L) // 2 ms per section
        val logs = mutableListOf<String>()
        val pm = PerformanceMonitor(taskId = "T2", log = { logs.add(it) }, timeSource = time)

        pm.startSection("db-call")
        pm.endSection("db-call")

        val bottlenecks = pm.analyzeBottlenecks(1_000_000L)
        assertTrue(bottlenecks.any { it.contains("db-call") })
    }

    @Test
    fun testLogsContainTaskId() {
        val time = FakeTimeSource(0L, 1)
        val logs = mutableListOf<String>()
        val pm = PerformanceMonitor(taskId = "G8-05-CP", log = { logs.add(it) }, timeSource = time)

        pm.startSection("a")
        pm.endSection("a")

        val hasTaskId = logs.any { it.contains("[task_id=G8-05-CP]") }
        assertTrue(hasTaskId)
    }
}
