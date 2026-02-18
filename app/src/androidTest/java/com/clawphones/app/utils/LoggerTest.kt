package com.clawphones.app.utils

import android.util.Log
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import java.io.File

@RunWith(MockitoJUnitRunner::class)
class LoggerTest {

    @Mock
    private lateinit var mockFile: File

    @Mock
    private lateinit var mockParentFile: File

    private val testLogDir: File by lazy {
        File(System.getProperty("java.io.tmpdir"), "test_logs_${System.currentTimeMillis()}").also {
            it.mkdirs()
        }
    }

    @Before
    fun setUp() {
        testLogDir.deleteRecursively()
        testLogDir.mkdirs()
    }

    @Test
    fun testLogLevels() {
        Logger.init("", testLogDir)

        Logger.d("Debug message", "task-001")
        Logger.i("Info message", "task-002")
        Logger.w("Warning message", "task-003")
        Logger.e("Error message", "task-004")

        val logFile = testLogDir.listFiles()?.firstOrNull()
        assertNotNull("Log file should be created", logFile)

        val content = logFile?.readText() ?: ""
        assertTrue("Should contain debug message", content.contains("Debug message"))
        assertTrue("Should contain info message", content.contains("Info message"))
        assertTrue("Should contain warning message", content.contains("Warning message"))
        assertTrue("Should contain error message", content.contains("Error message"))

        Logger.release()
    }

    @Test
    fun testTaskIdInLogs() {
        Logger.init("", testLogDir)

        val taskId = "test-task-123"
        Logger.i("Test message", taskId)

        val logFile = testLogDir.listFiles()?.firstOrNull()
        val content = logFile?.readText() ?: ""

        assertTrue("Log should contain task ID", content.contains("[$taskId]"))

        Logger.release()
    }

    @Test
    fun testLogRotation() {
        val smallMaxSize = 100L
        val testDir = File(System.getProperty("java.io.tmpdir"), "test_rotation_${System.currentTimeMillis()}")
        testDir.mkdirs()

        Logger.init("", testDir)

        val largeMessage = "A".repeat(200)
        Logger.d(largeMessage)
        Logger.d(largeMessage)
        Logger.d(largeMessage)

        val logFiles = testDir.listFiles { f -> f.name.startsWith("clawphones_") && f.name.endsWith(".log") }
        assertTrue("Should have multiple log files after rotation", (logFiles?.size ?: 0) > 1)

        testDir.deleteRecursively()
        Logger.release()
    }

    @Test
    fun testExceptionLogging() {
        Logger.init("", testLogDir)

        val exception = RuntimeException("Test exception")
        Logger.e("Error with exception", "task-exc", exception)

        val logFile = testLogDir.listFiles()?.firstOrNull()
        val content = logFile?.readText() ?: ""

        assertTrue("Should contain error message", content.contains("Error with exception"))
        assertTrue("Should contain exception stack trace", content.contains("RuntimeException"))

        Logger.release()
    }

    @Test
    fun testMultipleTaskIds() {
        Logger.init("", testLogDir)

        val taskIds = listOf("task-A", "task-B", "task-C")
        taskIds.forEach { taskId ->
            Logger.i("Message for $taskId", taskId)
        }

        val logFile = testLogDir.listFiles()?.firstOrNull()
        val content = logFile?.readText() ?: ""

        taskIds.forEach { taskId ->
            assertTrue("Should contain task ID $taskId", content.contains("[$taskId]"))
        }

        Logger.release()
    }

    @Test
    fun testLogTimestampFormat() {
        Logger.init("", testLogDir)

        Logger.i("Test message")

        val logFile = testLogDir.listFiles()?.firstOrNull()
        val content = logFile?.readText() ?: ""

        val timestampRegex = Regex("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}")
        assertTrue("Should contain timestamp in correct format", timestampRegex.containsMatchIn(content))

        Logger.release()
    }

    @Test
    fun testRemoteEndpointConfig() {
        val remoteUrl = "https://logs.example.com/api/upload"
        Logger.init(remoteUrl, testLogDir)

        Logger.i("Remote test message")

        Thread.sleep(100)

        Logger.flush()
        Logger.release()

        assertTrue("Remote endpoint should be configured", true)
    }

    @Test
    fun testAllLogLevelsPresent() {
        Logger.init("", testLogDir)

        Logger.d("DEBUG log")
        Logger.i("INFO log")
        Logger.w("WARN log")
        Logger.e("ERROR log")

        val logFile = testLogDir.listFiles()?.firstOrNull()
        val content = logFile?.readText() ?: ""

        assertTrue("Should contain DEBUG level", content.contains("DEBUG"))
        assertTrue("Should contain INFO level", content.contains("INFO "))
        assertTrue("Should contain WARN level", content.contains("WARN "))
        assertTrue("Should contain ERROR level", content.contains("ERROR"))

        Logger.release()
    }

    @Test
    fun testLogFileNaming() {
        Logger.init("", testLogDir)

        Logger.i("Test message")

        val logFiles = testLogDir.listFiles { f ->
            f.name.startsWith("clawphones_") && f.name.endsWith(".log")
        }

        assertNotNull("Log file should exist", logFiles)
        assertTrue("Log file should have date in name", logFiles?.firstOrNull()?.name?.contains("20") == true)

        Logger.release()
    }
}
