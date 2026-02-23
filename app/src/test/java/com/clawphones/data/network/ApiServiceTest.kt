package com.clawphones.data.network

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.fail
import kotlinx.coroutines.runBlocking
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiServiceTest {
    private lateinit var server: MockWebServer

    @Before
    fun setup() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun loginSuccess() = runBlocking {
        val body = "{\"token\":\"tok\",\"userId\":\"u1\"}"
        server.enqueue(MockResponse().setResponseCode(200).setBody(body))

        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val endpoints = retrofit.create(ApiEndpoints::class.java)
        val client = ApiClient(endpoints)

        val resp = client.login(LoginRequest("alice", "pwd"))
        assertEquals("tok", resp.token)
        assertEquals("u1", resp.userId)

        val request = server.takeRequest()
        assertEquals("/login", request.path)
    }

    @Test
    fun loginFailureThrows() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(401).setBody("{\"error\":\"unauthorized\"}"))

        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val endpoints = retrofit.create(ApiEndpoints::class.java)
        val client = ApiClient(endpoints)

        try {
            client.login(LoginRequest("alice", "pwd"))
            fail("Expected ApiException")
        } catch (e: ApiException) {
            assertEquals(401, e.code)
        }
    }

    @Test
    fun registerSuccess() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(200).setBody("{\"userId\":\"u2\",\"token\":\"tok2\"}"))

        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val endpoints = retrofit.create(ApiEndpoints::class.java)
        val client = ApiClient(endpoints)

        val resp = client.register(RegisterRequest("bob", "pwd", "bob@example.com"))
        assertEquals("u2", resp.userId)
        assertEquals("tok2", resp.token)
    }

    @Test
    fun registerFailureThrows() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(400).setBody("{\"error\":\"bad\"}"))

        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val endpoints = retrofit.create(ApiEndpoints::class.java)
        val client = ApiClient(endpoints)

        try {
            client.register(RegisterRequest("bob", "pwd", "bob@example.com"))
            fail("Expected ApiException")
        } catch (e: ApiException) {
            assertEquals(400, e.code)
        }
    }
}
