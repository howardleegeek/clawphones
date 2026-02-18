package com.clawphones.monitoring

/**
 * Lightweight monitoring module abstraction for Firebase Crashlytics integration.
 * This is a test-friendly shim that allows injecting a Crashlytics service and
 * reporting crashes with contextual task IDs.
 */
interface CrashlyticsService {
    fun logError(e: Throwable, message: String? = null, taskId: String? = null)
    fun logInfo(message: String, taskId: String? = null)
}

class DefaultCrashlyticsService : CrashlyticsService {
    override fun logError(e: Throwable, message: String?, taskId: String?) {
        val ctx = if (taskId != null) "[task:$taskId] " else ""
        println("Crashlytics Error: ${ctx}${message ?: ""} ${e.message}")
        // In a real app: FirebaseCrashlytics.getInstance().recordException(e)
    }

    override fun logInfo(message: String, taskId: String?) {
        val ctx = if (taskId != null) "[task:$taskId] " else ""
        println("Crashlytics Info: ${ctx}$message")
    }
}

/** Global monitoring module with optional task-scoped context. */
object MonitoringModule {
    @Volatile
    private var initialized = false
    lateinit var crashlytics: CrashlyticsService
    /** Current task context for logging. */
    var currentTaskId: String? = null

    /** Initialize with an optional Crashlytics service and task context. */
    @JvmStatic
    fun initialize(service: CrashlyticsService? = null, taskId: String? = null) {
        crashlytics = service ?: DefaultCrashlyticsService()
        currentTaskId = taskId
        initialized = true
        logInfo("MonitoringModule initialized")
    }

    /** Convenience log with current context. */
    private fun logInfo(message: String) {
        if (!initialized) throw IllegalStateException("MonitoringModule not initialized")
        crashlytics.logInfo(message, currentTaskId)
    }

    /** Report a crash to Crashlytics with optional message and current context. */
    fun reportCrash(e: Throwable, message: String? = null) {
        if (!initialized) throw IllegalStateException("MonitoringModule not initialized")
        crashlytics.logError(e, message, currentTaskId)
    }
}
