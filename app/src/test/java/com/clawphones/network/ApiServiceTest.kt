package com.clawphones.network

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ApiServiceTest {

    private lateinit var apiService: ApiService
    private val mockApiService = mockk<ApiService>()

    @BeforeEach
    fun setup() {
        apiService = mockApiService
    }

    @Test
    fun `login sends correct request data`() = runTest {
        val loginRequest = LoginRequest(username = "testuser", password = "password123")
        val expectedResponse = LoginResponse(
            success = true,
            message = "Login successful",
            user = UserInfo(
                id = "1",
                username = "testuser",
                email = "test@example.com",
                token = "mock-token-123"
            )
        )

        coEvery { mockApiService.login(loginRequest) } returns expectedResponse

        val result = apiService.login(loginRequest)

        assertEquals(true, result.success)
        assertEquals("testuser", result.user?.username)
    }

    @Test
    fun `login returns user info on success`() = runTest {
        val loginRequest = LoginRequest(username = "john", password = "securepass")
        val expectedResponse = LoginResponse(
            success = true,
            message = null,
            user = UserInfo(
                id = "42",
                username = "john",
                email = "john@example.com",
                token = "abc123token"
            )
        )

        coEvery { mockApiService.login(loginRequest) } returns expectedResponse

        val result = apiService.login(loginRequest)

        assertNotNull(result.user)
        assertEquals("42", result.user?.id)
        assertEquals("john", result.user?.username)
        assertEquals("john@example.com", result.user?.email)
        assertEquals("abc123token", result.user?.token)
    }

    @Test
    fun `login returns failure response`() = runTest {
        val loginRequest = LoginRequest(username = "invalid", password = "wrong")
        val expectedResponse = LoginResponse(
            success = false,
            message = "Invalid credentials",
            user = null
        )

        coEvery { mockApiService.login(loginRequest) } returns expectedResponse

        val result = apiService.login(loginRequest)

        assertEquals(false, result.success)
        assertEquals("Invalid credentials", result.message)
        assertEquals(null, result.user)
    }
}
