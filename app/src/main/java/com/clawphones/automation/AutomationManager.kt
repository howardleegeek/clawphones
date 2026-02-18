package com.clawphones.automation

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

// Core orchestrator for automation tasks
class AutomationManager(private val scheduler: Scheduler) {
    fun scheduleDailyBackup(delayHours: Long = 0L) {
        val request = createDailyBackupWorkRequest(delayHours)
        scheduler.schedulePeriodicWork("daily_backup", request)
    }

    fun cancelDailyBackup() {
        scheduler.cancelPeriodicWork("daily_backup")
    }

    companion object {
        // Exposed for unit tests to validate the created WorkRequest without needing a real DB/WorkManager
        fun createDailyBackupWorkRequest(initialDelayHours: Long = 0L): PeriodicWorkRequest {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()
            return PeriodicWorkRequestBuilder<BackupWorker>(24, TimeUnit.HOURS)
                .setConstraints(constraints)
                .setInitialDelay(initialDelayHours, TimeUnit.HOURS)
                .build()
        }
    }
}

// Scheduler abstraction to enable unit testing
interface Scheduler {
    fun schedulePeriodicWork(name: String, request: PeriodicWorkRequest)
    fun cancelPeriodicWork(name: String)
}

// WorkManager-based implementation
class WorkManagerScheduler(private val context: Context) : Scheduler {
    override fun schedulePeriodicWork(name: String, request: PeriodicWorkRequest) {
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            name,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    override fun cancelPeriodicWork(name: String) {
        WorkManager.getInstance(context).cancelUniqueWork(name)
    }
}

// Simple BackupWorker to illustrate scheduled task (no real I/O in this stub)
class BackupWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        return try {
            // Placeholder for actual backup logic
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
