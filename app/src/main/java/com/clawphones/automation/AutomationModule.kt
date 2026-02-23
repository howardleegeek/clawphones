package com.clawphones.automation

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

// Simple data container for automation task configuration
data class AutomationTaskConfig(val id: String, val taskType: String, val intervalHours: Long)

// Worker that represents a generic automation task. In a real app this would perform
// the actual work (sync, backup, etc.). Here we simulate with a log line and success.
class AutomationWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val taskId = inputData.getString("TASK_ID") ?: "unknown"
        val taskType = inputData.getString("TASK_TYPE") ?: "UNKNOWN"
        // Contextual log for traceability
        println("AutomationWorker [task_id=$taskId, type=$taskType] executing")
        // Simulated work completed
        return Result.success()
    }
}

class AutomationModule {
    companion object {
        // Build WorkRequests from given configs. This is testable without Android UI.
        fun buildWorkRequests(configs: List<AutomationTaskConfig>): List<WorkRequest> {
            val requests = mutableListOf<WorkRequest>()
            for (config in configs) {
                val data = workDataOf(
                    "TASK_ID" to config.id,
                    "TASK_TYPE" to config.taskType
                )
                val req: WorkRequest = if (config.intervalHours > 0) {
                    PeriodicWorkRequestBuilder<AutomationWorker>(config.intervalHours, TimeUnit.HOURS)
                        .setInputData(data)
                        .build()
                } else {
                    OneTimeWorkRequestBuilder<AutomationWorker>()
                        .setInputData(data)
                        .build()
                }
                requests.add(req)
            }
            return requests
        }

        // Schedule the automation tasks using WorkManager
        fun scheduleTasks(context: Context, configs: List<AutomationTaskConfig>) {
            val workManager = WorkManager.getInstance(context)
            val requests = buildWorkRequests(configs)
            // Enqueue with deterministic unique names per task index
            for ((idx, req) in requests.withIndex()) {
                workManager.enqueueUniqueWork("automation_task_${idx}", ExistingWorkPolicy.KEEP, req)
            }
        }
    }
}
