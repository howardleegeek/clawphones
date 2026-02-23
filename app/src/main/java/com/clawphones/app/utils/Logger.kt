package com.clawphones.app.utils

import android.util.Log
import timber.log.Timber
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class Logger private constructor() {

    companion object {
        private const val TIMBER_TAG = "ClawPhones"
        private const val MAX_LOG_SIZE = 10 * 1024 * 1024L
        private const val MAX_LOG_FILES = 5
        private const val REMOTE_BATCH_SIZE = 10
        private const val REMOTE_FLUSH_INTERVAL_MS = 5000L

        @Volatile
        private var isInitialized = false

        @Volatile
        private var logFile: File? = null

        @Volatile
        private var remoteEndpoint: String = ""

        private val executor = Executors.newSingleThreadExecutor()
        private val logBuffer = mutableListOf<LogEntry>()
        private val bufferLock = Any()

        fun init(remoteEndpoint: String = "", logDir: File? = null) {
            if (isInitialized) return

            this.remoteEndpoint = remoteEndpoint

            if (BuildConfig.DEBUG) {
                Timber.plant(Timber.DebugTree())
            }

            logDir?.let { dir ->
                setupFileLogging(dir)
            }

            if (remoteEndpoint.isNotEmpty()) {
                setupRemoteLogging()
            }

            isInitialized = true
            d("Logger initialized. Remote: $remoteEndpoint")
        }

        private fun setupFileLogging(logDir: File) {
            if (!logDir.exists()) {
                logDir.mkdirs()
            }
            logFile = getCurrentLogFile(logDir)
            rotateLogsIfNeeded(logDir)
        }

        private fun getCurrentLogFile(logDir: File): File {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val timestamp = dateFormat.format(Date())
            return File(logDir, "clawphones_$timestamp.log")
        }

        private fun rotateLogsIfNeeded(logDir: File) {
            logFile?.let { file ->
                if (file.exists() && file.length() > MAX_LOG_SIZE) {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)
                    val timestamp = dateFormat.format(Date())
                    val rotatedFile = File(logDir, "clawphones_${timestamp}.log")
                    file.renameTo(rotatedFile)

                    cleanupOldLogs(logDir)
                }
            }
        }

        private fun cleanupOldLogs(logDir: File) {
            val logFiles = logDir.listFiles { f ->
                f.name.startsWith("clawphones_") && f.name.endsWith(".log")
            }?.sortedByDescending { it.lastModified() } ?: return

            if (logFiles.size > MAX_LOG_FILES) {
                logFiles.drop(MAX_LOG_FILES).forEach { it.delete() }
            }
        }

        private fun setupRemoteLogging() {
            executor.scheduleAtFixedRate(
                { flushLogsToRemote() },
                REMOTE_FLUSH_INTERVAL_MS,
                REMOTE_FLUSH_INTERVAL_MS,
                TimeUnit.MILLISECONDS
            )
        }

        fun d(message: String, taskId: String = "") {
            log(DEBUG, message, taskId)
        }

        fun i(message: String, taskId: String = "") {
            log(INFO, message, taskId)
        }

        fun w(message: String, taskId: String = "") {
            log(WARN, message, taskId)
        }

        fun e(message: String, taskId: String = "", throwable: Throwable? = null) {
            log(ERROR, message, taskId, throwable)
        }

        private fun log(level: LogLevel, message: String, taskId: String, throwable: Throwable? = null) {
            val tag = TIMBER_TAG
            val formattedMessage = if (taskId.isNotEmpty()) "[$taskId] $message" else message

            when (level) {
                DEBUG -> Timber.tag(tag).d(formattedMessage)
                INFO -> Timber.tag(tag).i(formattedMessage)
                WARN -> Timber.tag(tag).w(formattedMessage)
                ERROR -> {
                    if (throwable != null) {
                        Timber.tag(tag).e(throwable, formattedMessage)
                    } else {
                        Timber.tag(tag).e(formattedMessage)
                    }
                }
            }

            writeToFile(level, formattedMessage, throwable)
            bufferForRemote(level, formattedMessage, throwable)
        }

        private fun writeToFile(level: LogLevel, message: String, throwable: Throwable?) {
            logFile?.let { file ->
                try {
                    synchronized(file) {
                        rotateLogsIfNeeded(file.parentFile ?: return)
                        PrintWriter(FileWriter(file, true)).use { writer ->
                            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(Date())
                            val levelStr = level.name.padEnd(5)
                            val throwableStr = if (throwable != null) "\n${Log.getStackTraceString(throwable)}" else ""
                            writer.println("$timestamp $levelStr $message$throwableStr")
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TIMBER_TAG, "Failed to write to log file", e)
                }
            }
        }

        private fun bufferForRemote(level: LogLevel, message: String, throwable: Throwable?) {
            if (remoteEndpoint.isEmpty()) return

            val entry = LogEntry(
                timestamp = System.currentTimeMillis(),
                level = level.name,
                message = message,
                stackTrace = throwable?.let { Log.getStackTraceString(it) }
            )

            synchronized(bufferLock) {
                logBuffer.add(entry)
                if (logBuffer.size >= REMOTE_BATCH_SIZE) {
                    flushLogsToRemote()
                }
            }
        }

        private fun flushLogsToRemote() {
            val entriesToSend: List<LogEntry>
            synchronized(bufferLock) {
                if (logBuffer.isEmpty()) return
                entriesToSend = logBuffer.toList()
                logBuffer.clear()
            }

            if (remoteEndpoint.isEmpty()) return

            try {
                val jsonPayload = buildJsonPayload(entriesToSend)
                sendToRemote(jsonPayload)
            } catch (e: Exception) {
                Log.e(TIMBER_TAG, "Failed to send logs to remote", e)
            }
        }

        private fun buildJsonPayload(entries: List<LogEntry>): String {
            val sb = StringBuilder()
            sb.append("{\"logs\":[")
            entries.forEachIndexed { index, entry ->
                if (index > 0) sb.append(",")
                sb.append("{\"timestamp\":${entry.timestamp},\"level\":\"${entry.level}\",\"message\":\"${escapeJson(entry.message)}\",\"stackTrace\":\"${escapeJson(entry.stackTrace ?: "")}\"}")
            }
            sb.append("]}")
            return sb.toString()
        }

        private fun escapeJson(s: String): String {
            return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
        }

        private fun sendToRemote(jsonPayload: String) {
            try {
                val url = java.net.URL(remoteEndpoint)
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.doOutput = true
                connection.outputStream.use { os ->
                    os.write(jsonPayload.toByteArray(Charsets.UTF_8))
                }
                val responseCode = connection.responseCode
                if (responseCode !in 200..299) {
                    Log.w(TIMBER_TAG, "Remote log upload failed with code: $responseCode")
                }
                connection.disconnect()
            } catch (e: Exception) {
                Log.e(TIMBER_TAG, "Failed to send logs to remote", e)
            }
        }

        fun flush() {
            flushLogsToRemote()
        }

        fun release() {
            flush()
            executor.shutdown()
            try {
                executor.awaitTermination(5, TimeUnit.SECONDS)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }
            Timber.uprootAll()
        }
    }

    enum class LogLevel {
        DEBUG, INFO, WARN, ERROR
    }

    data class LogEntry(
        val timestamp: Long,
        val level: String,
        val message: String,
        val stackTrace: String?
    )
}
