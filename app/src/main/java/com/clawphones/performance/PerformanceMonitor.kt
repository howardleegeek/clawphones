package com.clawphones.performance

/**
 * Lightweight, JVM-friendly performance monitor.
 * Collects per-section durations and exposes a simple report/analyze API.
 * Logs include task context when provided.
 */
class PerformanceMonitor(
    private val taskId: String? = null,
    private val log: (String) -> Unit = { println(it) },
    private val timeSource: TimeSource = SystemTimeSource()
) {

    interface TimeSource {
        fun now(): Long
    }

    class SystemTimeSource : TimeSource {
        override fun now(): Long = System.nanoTime()
    }

    data class SectionMetrics(val name: String, var totalNanos: Long = 0L, var count: Int = 0)

    private data class ActiveSection(var startNanos: Long)

    private val sections = mutableMapOf<String, SectionMetrics>()
    private val activeSections = mutableMapOf<String, ActiveSection>()

    fun startSection(name: String) {
        val now = timeSource.now()
        activeSections[name] = ActiveSection(now)
        logContext("Started section '$name' at ${now}ns")
    }

    fun endSection(name: String) {
        val now = timeSource.now()
        val a = activeSections.remove(name)
        if (a != null) {
            val duration = now - a.startNanos
            val s = sections.getOrPut(name) { SectionMetrics(name) }
            s.totalNanos += duration
            s.count += 1
            logContext("Ended section '$name' duration=${duration}ns")
        } else {
            logContext("Warning: endSection called for '$name' without startSection", isError = true)
        }
    }

    data class PerformanceReport(val totalNanos: Long, val topSection: String?, val details: Map<String, Long>)

    fun report(): PerformanceReport {
        val total = sections.values.sumOf { it.totalNanos }
        val top = sections.values.maxByOrNull { it.totalNanos }?.name
        val map = sections.values.associate { it.name to it.totalNanos }
        return PerformanceReport(total, top, map)
    }

    fun analyzeBottlenecks(thresholdNanos: Long = 1_000_000L): List<String> {
        return sections.values
            .filter { it.totalNanos >= thresholdNanos }
            .map { "${it.name} took ${it.totalNanos / 1_000_000} ms" }
    }

    private fun logContext(msg: String, isError: Boolean = false) {
        val m = if (taskId != null) "[task_id=$taskId] $msg" else msg
        val level = if (isError) "ERROR" else "INFO"
        log("[$level] $m")
    }
}
