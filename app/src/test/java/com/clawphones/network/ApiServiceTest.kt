package com.clawphones.network

import java.io.IOException
import java.util.concurrent.TimeUnit
import okhttp3.Call
import okhttp3.Connection
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class ApiServiceTest {

    @Test
    fun retriesThreeTimesBeforeSuccess() {
        val delays = mutableListOf<Long>()
        val interceptor = RetryInterceptor(sleeper = { delays.add(it) })
        val chain = FakeChain(
            listOf(
                Result.failure(IOException("fail-1")),
                Result.failure(IOException("fail-2")),
                Result.failure(IOException("fail-3")),
                Result.success(createResponse(200))
            )
        )

        val response = interceptor.intercept(chain)

        assertEquals(200, response.code)
        assertEquals(4, chain.proceedCount)
        assertEquals(listOf(1_000L, 1_000L, 1_000L), delays)
    }

    @Test
    fun throwsAfterMaxRetries() {
        val delays = mutableListOf<Long>()
        val interceptor = RetryInterceptor(sleeper = { delays.add(it) })
        val chain = FakeChain(
            listOf(
                Result.failure(IOException("fail-1")),
                Result.failure(IOException("fail-2")),
                Result.failure(IOException("fail-3")),
                Result.failure(IOException("fail-4"))
            )
        )

        try {
            interceptor.intercept(chain)
            fail("Expected IOException")
        } catch (exception: IOException) {
            assertTrue(exception.message?.contains("fail-4") == true)
        }
        assertEquals(4, chain.proceedCount)
        assertEquals(listOf(1_000L, 1_000L, 1_000L), delays)
    }

    @Test
    fun retriesOnHttpErrorResponse() {
        val delays = mutableListOf<Long>()
        val interceptor = RetryInterceptor(sleeper = { delays.add(it) })
        val chain = FakeChain(
            listOf(
                Result.success(createResponse(500)),
                Result.success(createResponse(500)),
                Result.success(createResponse(500)),
                Result.success(createResponse(500))
            )
        )

        val response = interceptor.intercept(chain)

        assertEquals(500, response.code)
        assertEquals(4, chain.proceedCount)
        assertEquals(listOf(1_000L, 1_000L, 1_000L), delays)
    }

    private class FakeChain(private val results: List<Result<Response>>) : Interceptor.Chain {
        private var index = 0
        var proceedCount = 0
            private set

        private val request = Request.Builder().url("https://example.com/").build()

        override fun request(): Request = request

        override fun proceed(request: Request): Response {
            proceedCount += 1
            val result = results[index]
            index += 1
            return result.getOrElse { throw it as IOException }
        }

        override fun call(): Call {
            throw UnsupportedOperationException("Not required for this test")
        }

        override fun connection(): Connection? = null

        override fun connectTimeoutMillis(): Int = 0

        override fun withConnectTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain = this

        override fun readTimeoutMillis(): Int = 0

        override fun withReadTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain = this

        override fun writeTimeoutMillis(): Int = 0

        override fun withWriteTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain = this
    }

    @Test
    fun doesNotRetryOnClientError4xx() {
        val delays = mutableListOf<Long>()
        val interceptor = RetryInterceptor(sleeper = { delays.add(it) })
        val chain = FakeChain(
            listOf(
                Result.success(createResponse(404))
            )
        )

        val response = interceptor.intercept(chain)

        // Should not retry on 4xx errors
        assertEquals(404, response.code)
        assertEquals(1, chain.proceedCount)
        assertTrue(delays.isEmpty())
    }
}

private fun createResponse(code: Int): Response {
    val request = Request.Builder().url("https://example.com/").build()
    return Response.Builder()
        .request(request)
        .protocol(Protocol.HTTP_1_1)
        .code(code)
        .message("message-$code")
        .build()
}
