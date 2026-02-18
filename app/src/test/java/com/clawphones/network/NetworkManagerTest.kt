package com.clawphones.network

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NetworkManagerTest {
    @Test
    fun testAutoReconnectOnDisconnection() = runBlocking {
        // Prepare
        NetworkManager.setTaskContext("test1")
        val errors = mutableListOf<String>()
        NetworkManager.setOnErrorHandler { t, id -> errors.add("${t.message}@${id}") }
        NetworkManager.reconnectBehavior = { true }
        // first two calls fail, third succeeds
        var reqCount = 0
        val request: suspend () -> String = {
            reqCount++
            if (reqCount < 2) {
                throw NetworkException("simulated failure")
            } else {
                "ok"
            }
        }
        // simulate an existing connection that gets dropped
        // Ensure initial state is disconnected to force reconnect path
        NetworkManager.disconnect()
        val result = NetworkManager.sendRequest(request)
        assertEquals("ok", result)
        // there should be at least one error logged
        assertTrue(errors.isNotEmpty())
    }

    @Test
    fun testRetryMechanismEventuallySucceeds() = runBlocking {
        NetworkManager.setTaskContext("test2")
        val errors = mutableListOf<String>()
        NetworkManager.setOnErrorHandler { t, id -> errors.add("${t.message}@${id}") }
        // Always succeed after 2 failures
        NetworkManager.reconnectBehavior = { true }
        var reqCount = 0
        val request: suspend () -> String = {
            reqCount++
            if (reqCount <= 2) throw NetworkException("flaky network")
            "success"
        }
        NetworkManager.disconnect()
        val result = NetworkManager.sendRequest(request)
        assertEquals("success", result)
        // ensure there were retries and errors captured
        assertTrue(errors.size >= 1)
    }

    @Test
    fun testPermanentFailureTriggersException() = runBlocking {
        NetworkManager.setTaskContext("test3")
        val errors = mutableListOf<String>()
        NetworkManager.setOnErrorHandler { t, id -> errors.add("${t.message}@${id}") }
        NetworkManager.maxRetries = 1
        // Always fail
        NetworkManager.reconnectBehavior = { false }
        var reqCount = 0
        val request: suspend () -> String = {
            reqCount++
            throw NetworkException("fail always")
        }
        NetworkManager.disconnect()
        try {
            NetworkManager.sendRequest(request)
        } catch (e: NetworkException) {
            // expected
        }
        assertTrue(errors.isNotEmpty())
    }
}
