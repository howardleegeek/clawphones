package com.clawphones.monitoring

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

// Data structure representing a performance sample
data class PerformanceMetric(
    val cpuUsagePercent: Float,
    val memoryUsageMB: Long,
    val batteryLevel: Int,
    val timestampMs: Long = System.currentTimeMillis()
)

// Providers for data sources. These can be implemented against Android APIs in the app,
// or mocked in tests.
interface CpuUsageProvider {
    suspend fun getCpuUsagePercent(): Float
}

interface MemoryUsageProvider {
    suspend fun getMemoryUsageMB(): Long
}

interface BatteryProvider {
    suspend fun getBatteryLevel(): Int
}

// Reporter interface to push metrics to an external monitoring tool (e.g., Firebase Performance)
interface PerformanceReporter {
    suspend fun report(metric: PerformanceMetric)
}

/**
 * PerformanceMonitor collects CPU, memory and battery usage at a fixed interval
 * and reports each sample to a provided reporter.
 */
class PerformanceMonitor(
    private val cpuProvider: CpuUsageProvider,
    private val memoryProvider: MemoryUsageProvider,
    private val batteryProvider: BatteryProvider,
    private val reporter: PerformanceReporter
) {
    // Fallback no-op reporter to simplify instantiation when a real reporter isn't provided
    private object NoOpReporter : PerformanceReporter {
        override suspend fun report(metric: PerformanceMetric) {
            // intentionally empty
        }
    }

    /**
     * Secondary constructor for convenience when a reporter isn't required yet.
     * It wires in a no-op reporter to avoid null checks in calling code.
     */
    constructor(
        cpuProvider: CpuUsageProvider,
        memoryProvider: MemoryUsageProvider,
        batteryProvider: BatteryProvider
    ) : this(cpuProvider, memoryProvider, batteryProvider, NoOpReporter)
    /**
     * Starts monitoring and returns a Flow of PerformanceMetric samples.
     * The Flow emits indefinitely until collected with a terminal operator.
     */
    fun startMonitoring(intervalMs: Long = 1000L): Flow<PerformanceMetric> {
        return flow {
            while (true) {
                val cpu = try {
                    cpuProvider.getCpuUsagePercent()
                } catch (e: Throwable) {
                    0f
                }
                val mem = try {
                    memoryProvider.getMemoryUsageMB()
                } catch (e: Throwable) {
                    0L
                }
                val batt = try {
                    batteryProvider.getBatteryLevel()
                } catch (e: Throwable) {
                    -1
                }
                val metric = PerformanceMetric(cpu, mem, batt, System.currentTimeMillis())
                try {
                    reporter.report(metric)
                } catch (e: Throwable) {
                    // Log with context for easier debugging in CI/dev environments
                    println("PerformanceMonitor: reporter failed at ${metric.timestampMs}: ${e.message}")
                }
                emit(metric)
                delay(intervalMs)
            }
        }.flowOn(Dispatchers.Default)
    }
}
