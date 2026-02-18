package com.clawphones.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

// Lightweight network manager with auto-reconnect and retry support.
// Note: This is a simplified, test-friendly implementation designed for kata-style tests.
object NetworkManager {
    // internal connection state
    private val connected = AtomicBoolean(false)

    // task-scoped context for logging
    private var currentTaskId: String? = null

    // error callback: (Throwable, taskId) -> Unit
    private var onErrorCallback: ((Throwable, String) -> Unit)? = null

    // customizeable hooks for tests
    var reconnectBehavior: (() -> Boolean)? = null

    // retry policy (configurable for tests)
    var maxRetries: Int = 2
    var baseDelayMs: Long = 1L

    fun setTaskContext(taskId: String) {
        currentTaskId = taskId
    }

    fun setOnErrorHandler(handler: (Throwable, String) -> Unit) {
        onErrorCallback = handler
    }

    suspend fun connect(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // simulate a short connect delay
                delay(50)
                connected.set(true)
                log("connected")
                true
            } catch (e: Exception) {
                onErrorCallback?.invoke(e, currentTaskId ?: "")
                connected.set(false)
                false
            }
        }
    }

    suspend fun disconnect() {
        withContext(Dispatchers.IO) {
            connected.set(false)
            log("disconnected")
        }
    }

    // Core API: run a suspending request with automatic retry and reconnect
    suspend fun <T> sendRequest(request: suspend () -> T): T {
        // Ensure we are connected or attempt reconnect
        if (!connected.get()) {
            val ok = attemptReconnect()
            if (!ok) {
                throw NetworkException("Unable to reconnect before request")
            }
        }

        var lastError: Throwable? = null
        var attempt = 0
        while (true) {
            try {
                return request()
            } catch (e: Throwable) {
                lastError = e
                onErrorCallback?.invoke(e, currentTaskId ?: "")
                if (attempt >= maxRetries) {
                    throw e
                }
                // backoff before retry
                val backoff = baseDelayMs * (1L shl attempt)
                dailyDelay(backoff)
                // try to reconnect before next attempt
                val ok = attemptReconnect()
                if (!ok) {
                    throw NetworkException("Reconnection failed on retry")
                }
                attempt++
            }
        }
    }

    private suspend fun dailyDelay(ms: Long) {
        delay(ms)
    }

    private suspend fun attemptReconnect(): Boolean {
        connected.set(false)
        log("attempting reconnect")
        val behavior = reconnectBehavior
        val result = behavior?.invoke() ?: run {
            // default: simulate a short reconnect delay and succeed
            delay(20)
            true
        }
        if (result) {
            connected.set(true)
            log("reconnected")
            return true
        } else {
            log("reconnect failed")
            connected.set(false)
            return false
        }
    }

    private fun log(message: String) {
        val id = currentTaskId?.let { "[$it] " } ?: ""
        println("${id}$message") // simple contextual logging
    }
}

class NetworkException(message: String) : Exception(message)
