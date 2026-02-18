package com.clawphones.network

import com.google.gson.Gson
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.coroutines.runBlocking
import java.io.IOException

class ApiServiceTest {
    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: ApiService

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        apiService = ApiService(retrofit)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun testRegisterSendsCorrectRequestAndParsesResponse() = runBlocking {
        val registerResponse = RegisterResponse(true, userId = "user-123", message = "Created")
        val responseBody = Gson().toJson(registerResponse)
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(responseBody))

        // Perform register call
        val result = apiService.register("alice", "alice@example.com", "secret")
        // Verify response
        assertNotNull(result)
        assertEquals(true, result.success)
        assertEquals("user-123", result.userId)

        // Inspect request data
        val recordedRequest = mockWebServer.takeRequest()
        assertEquals("POST", recordedRequest.method)
        assertEquals("/api/register", recordedRequest.path)
        val body = recordedRequest.body.readUtf8()
        val actualReq = Gson().fromJson(body, RegisterRequest::class.java)
        assertEquals("alice", actualReq.username)
        assertEquals("alice@example.com", actualReq.email)
        assertEquals("secret", actualReq.password)
    }
}
