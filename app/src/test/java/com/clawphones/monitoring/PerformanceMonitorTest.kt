package com.clawphones.monitoring

import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

// Fake providers for deterministic unit tests
class FakeCpuProvider(private val values: List<Float>) : CpuUsageProvider {
    private var idx = 0
    override suspend fun getCpuUsagePercent(): Float {
        val v = values[idx % values.size]
        idx++
        return v
    }
}

class FakeMemoryProvider(private val values: List<Long>) : MemoryUsageProvider {
    private var idx = 0
    override suspend fun getMemoryUsageMB(): Long {
        val v = values[idx % values.size]
        idx++
        return v
    }
}

class FakeBatteryProvider(private val values: List<Int>) : BatteryProvider {
    private var idx = 0
    override suspend fun getBatteryLevel(): Int {
        val v = values[idx % values.size]
        idx++
        return v
    }
}

class RecordingReporter : PerformanceReporter {
    val metrics = mutableListOf<PerformanceMetric>()
    override suspend fun report(metric: PerformanceMetric) {
        metrics.add(metric)
    }
}

class PerformanceMonitorTest {
    @Test
    fun collectsThreeMetricsAndReports() = runBlocking {
        val cpu = FakeCpuProvider(listOf(10f, 20f, 30f))
        val mem = FakeMemoryProvider(listOf(1000L, 2000L, 3000L))
        val bat = FakeBatteryProvider(listOf(50, 60, 70))
        val reporter = RecordingReporter()
        val monitor = PerformanceMonitor(cpu, mem, bat, reporter)

        val results = monitor.startMonitoring(1L).take(3).toList()

        // Verify that three samples were emitted
        assertEquals(3, results.size)
        // Verify that the reporter captured three reports
        assertEquals(3, reporter.metrics.size)
        // Check content of the first metric
        val first = results[0]
        assertEquals(10f, first.cpuUsagePercent, 0.001f)
        assertEquals(1000L, first.memoryUsageMB)
        assertEquals(50, first.batteryLevel)
    }
}
