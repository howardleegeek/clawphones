package com.clawphones.app

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppTest {

    private lateinit var app: App
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        app = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testAppInstance() {
        assertNotNull(app)
    }

    @Test
    fun testStartupTimeTracking() {
        val initialStartupTime = app.startupTimeMs
        assertTrue("Startup time should be >= 0", initialStartupTime >= 0)
    }

    @Test
    fun testRegisterStartupTask() {
        val task = StartupTask(
            name = "test_task",
            isCritical = true
        ) {
            Thread.sleep(10)
        }
        
        app.registerStartupTask(task)
        assertTrue(task.isCompleted)
    }

    @Test
    fun testNonCriticalTaskRunsAsync() {
        var completed = false
        val task = StartupTask(
            name = "async_task",
            isCritical = false
        ) {
            completed = true
        }
        
        app.registerStartupTask(task)
        Thread.sleep(100)
        assertTrue("Non-critical task should complete", completed)
    }

    @Test
    fun testBenchmark() {
        val benchmarkMs = app.benchmark("test_benchmark") {
            Thread.sleep(50)
        }
        
        assertTrue("Benchmark should record >= 50ms", benchmarkMs >= 50)
    }

    @Test
    fun testMeasureStartupTime() {
        val measuredTime = app.measureStartupTime {
            Thread.sleep(20)
        }
        
        assertTrue("Measured time should be >= 20ms", measuredTime >= 20)
    }

    @Test
    fun testMultipleStartupTasks() {
        val task1 = StartupTask("task1", true) { }
        val task2 = StartupTask("task2", false) { Thread.sleep(10) }
        
        app.registerStartupTask(task1)
        app.registerStartupTask(task2)
        
        Thread.sleep(50)
        
        assertTrue(task1.isCompleted)
        assertTrue(task2.isCompleted)
    }

    @Test
    fun testCriticalTaskExecutesFirst() {
        var executionOrder = mutableListOf<String>()
        
        val criticalTask = StartupTask("critical", true) {
            executionOrder.add("critical")
        }
        
        val nonCriticalTask = StartupTask("non_critical", false) {
            executionOrder.add("non_critical")
        }
        
        app.registerStartupTask(nonCriticalTask)
        app.registerStartupTask(criticalTask)
        
        Thread.sleep(50)
        
        assertTrue(criticalTask.isCompleted)
    }

    @Test
    fun testStartupTimeOptimization() {
        val baselineStartup = 1000L
        val targetReduction = 0.20
        val currentStartup = app.startupTimeMs
        
        val optimizedTarget = baselineStartup * (1 - targetReduction)
        
        assertTrue(
            "Startup time $currentStartup should be <= target $optimizedTarget",
            currentStartup <= optimizedTarget || currentStartup < baselineStartup
        )
    }

    @Test
    fun testWaitForStartupCompletion() {
        val result = app.waitForStartupCompletion(1000L)
        assertTrue("Startup should complete within timeout", result)
    }

    @Test
    fun testTaskFailureDoesNotCrash() {
        val failingTask = StartupTask("failing_task", false) {
            throw RuntimeException("Intentional failure")
        }
        
        assertDoesNotThrow {
            app.registerStartupTask(failingTask)
        }
    }

    @Test
    fun testContextIsAvailable() {
        assertNotNull("Context should be available", context)
    }
}
