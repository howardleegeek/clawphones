package com.clawphones.monitoring

import org.junit.Assert.*
import org.junit.Test

/** Tests for PerformanceMonitor. */
class PerformanceMonitorTest {

    // Fake trace implementation to capture metrics and stop state
    class FakeTrace(val metrics: MutableMap<String, Long> = mutableMapOf()) : PerformanceMonitor.Trace {
        var stopped = false
        override fun stop() { stopped = true }
        override fun incrementMetric(name: String, value: Long) { metrics[name] = value }
    }

    // Fake reporter to observe traces and logs without real Firebase/Timber
    class FakeReporter : PerformanceMonitor.Reporter {
        val traces = mutableListOf<FakeTrace>()
        val logs = mutableListOf<String>()
        override fun newTrace(name: String): PerformanceMonitor.Trace {
            val t = FakeTrace()
            traces.add(t)
            return t
        }
        override fun log(tag: String, message: String) {
            logs.add("$tag:$message")
        }
    }

    @Test
    fun testTraceStopsAndRecordsMetrics() {
        val fake = FakeReporter()
        PerformanceMonitor.init(fake)
        PerformanceMonitor.trace("testTrace") { t ->
            t.incrementMetric("db", 42)
        }
        // After trace ends, ensure the last trace was stopped and metrics recorded
        val last = fake.traces.last()
        assertTrue("Trace should be stopped after block completes", last.stopped)
        assertTrue("Metrics should contain 'db'", last.metrics.containsKey("db"))
        assertEquals("Metric value should be recorded", 42L, last.metrics["db"])
    }

    @Test
    fun testLoggingWithAndWithoutTaskContext() {
        // With task context
        val fake1 = FakeReporter()
        PerformanceMonitor.init(fake1)
        PerformanceMonitor.setTaskContext("ABC")
        PerformanceMonitor.logInfo("MyTag", "hello")
        assertEquals("MyTag:[task_id=ABC] hello", fake1.logs.last())

        // Without task context
        val fake2 = FakeReporter()
        PerformanceMonitor.init(fake2)
        PerformanceMonitor.clearTaskContext() // clear context
        PerformanceMonitor.logInfo("Other", "world")
        assertEquals("Other: world", fake2.logs.last())
    }
}
