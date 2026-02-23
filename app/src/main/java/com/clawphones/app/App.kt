package com.clawphones.app

import android.app.Application
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

class App : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val startupTasks = mutableListOf<StartupTask>()
    private val _startupTimeMs = AtomicLong(0)

    val startupTimeMs: Long get() = _startupTimeMs.get()

    companion object {
        private const val TAG = "ClawPhonesApp"
        private const val DEFAULT_STARTUP_TIMEOUT_MS = 5000L
    }

    override fun onCreate() {
        val startTime = System.currentTimeMillis()
        super.onCreate()
        
        initializeCriticalTasks()
        initializeNonCriticalTasksAsync()
        
        _startupTimeMs.set(System.currentTimeMillis() - startTime)
        Log.i(TAG, "App startup completed in ${_startupTimeMs.get()}ms")
    }

    private fun initializeCriticalTasks() {
        for (task in startupTasks) {
            if (task.isCritical) {
                task.execute()
            }
        }
    }

    private fun initializeNonCriticalTasksAsync() {
        applicationScope.launch {
            for (task in startupTasks) {
                if (!task.isCritical) {
                    launch {
                        task.execute()
                    }
                }
            }
        }
    }

    fun registerStartupTask(task: StartupTask) {
        startupTasks.add(task)
    }

    fun measureStartupTime(runnable: Runnable): Long {
        val startTime = System.nanoTime()
        runnable.run()
        val endTime = System.nanoTime()
        return TimeUnit.NANOSECONDS.toMillis(endTime - startTime)
    }

    fun benchmark(name: String, task: () -> Unit): Long {
        val startTime = System.currentTimeMillis()
        task()
        val elapsed = System.currentTimeMillis() - startTime
        Log.d(TAG, "Benchmark[$name]: ${elapsed}ms")
        return elapsed
    }

    fun waitForStartupCompletion(timeoutMs: Long = DEFAULT_STARTUP_TIMEOUT_MS): Boolean {
        val latch = CountDownLatch(1)
        var completed = false
        
        applicationScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    while (startupTasks.any { !it.isCompleted }) {
                        kotlinx.coroutines.delay(50)
                    }
                }
                completed = true
            } catch (e: Exception) {
                Log.e(TAG, "Startup wait failed: ${e.message}")
            } finally {
                latch.countDown()
            }
        }
        
        return latch.await(timeoutMs, TimeUnit.MILLISECONDS) && completed
    }
}

data class StartupTask(
    val name: String,
    val isCritical: Boolean = false,
    private val task: () -> Unit
) {
    @Volatile
    var isCompleted: Boolean = false
        private set

    fun execute() {
        try {
            task()
            isCompleted = true
            Log.d("StartupTask", "Completed: $name")
        } catch (e: Exception) {
            Log.e("StartupTask", "Failed: $name - ${e.message}")
            isCompleted = true
        }
    }
}
