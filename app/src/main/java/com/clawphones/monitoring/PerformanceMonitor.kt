package com.clawphones.monitoring

import com.google.firebase.perf.FirebasePerformance
import timber.log.Timber

/**
 * Centralized performance monitoring and logging helper.
 *
 * This class provides a lightweight wrapper around Firebase Performance Monitoring
 * and a logging facility via Timber. It also allows injecting a test-friendly
 * reporter to verify traces and logs in unit tests.
 */
object PerformanceMonitor {

    interface Trace {
        fun stop()
        fun incrementMetric(name: String, value: Long)
    }

    interface Reporter {
        fun newTrace(name: String): Trace
        fun log(tag: String, message: String)
    }

    // Real reporter using Firebase Performance and Timber
    private class RealReporter : Reporter {
        override fun newTrace(name: String): Trace {
            val firebaseTrace = FirebasePerformance.getInstance().newTrace(name)
            firebaseTrace.start()
            return object : Trace {
                override fun stop() {
                    firebaseTrace.stop()
                }
                override fun incrementMetric(name: String, value: Long) {
                    firebaseTrace.incrementMetric(name, value)
                }
            }
        }

        override fun log(tag: String, message: String) {
            Timber.tag(tag).d(message)
        }
    }

    private var reporter: Reporter = RealReporter()
    private var currentTaskId: String? = null

    /** Set a task context for logging (task_id). */
    fun setTaskContext(taskId: String) {
        currentTaskId = taskId
    }

    /** Clear the current task context so logs are not annotated. */
    fun clearTaskContext() {
        currentTaskId = null
    }

    /** Initialize with a custom reporter (for tests). */
    fun init(reporterOverride: Reporter? = null) {
        if (reporterOverride != null) reporter = reporterOverride
    }

    /** Run a traced block where the Trace can record metrics. */
    fun trace(name: String, block: (Trace) -> Unit) {
        val trace = reporter.newTrace(name)
        try {
            block(trace)
        } finally {
            trace.stop()
        }
    }

    /** Log a message with an optional task context. */
    fun log(tag: String, message: String) {
        val withContext = currentTaskId?.let { "[task_id=$it] " } ?: ""
        val finalMessage = withContext + message
        reporter.log(tag, finalMessage)
        Timber.tag(tag).d(finalMessage)
    }

    /** Convenience alias for standard info logging. */
    fun logInfo(tag: String, message: String) {
        log(tag, message)
    }
}
