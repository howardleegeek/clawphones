package com.clawphones.core

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class NetworkModuleTest {

    // Mock ApiService that can fail a specified number of times before succeeding
    class MockApiService(private var failTimes: Int) : ApiService {
        override suspend fun getData(): Data {
            if (failTimes > 0) {
                failTimes--
                throw IOException("Simulated network error")
            }
            return Data(1, "ok")
        }
    }

    @Test
    fun testFetchDataFlowRetriesAndSucceeds() = runBlocking {
        val api = MockApiService(2) // fail twice, then succeed
        val module = NetworkModule(api)
        val d = module.fetchDataFlow().first()
        assertEquals(1, d.id)
        assertEquals("ok", d.content)
    }

    @Test
    fun testFetchDataFlowFailsAfterRetries() = runBlocking {
        val api = MockApiService(5) // will always fail within 3 attempts
        val module = NetworkModule(api)
        try {
            module.fetchDataFlow().first()
            throw AssertionError("Expected exception not thrown")
        } catch (e: IOException) {
            // Expected after exhausting retries
        }
    }
}
