package com.clawphones.network

import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Response
import java.io.IOException

// Data models for register API
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

data class RegisterResponse(
    val success: Boolean,
    val userId: String? = null,
    val message: String? = null
)

// Retrofit API contract for registration endpoint
interface ApiRegisterService {
    @POST("/api/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>
}

class ApiService(private val retrofit: Retrofit) {
    private val api = retrofit.create(ApiRegisterService::class.java)

    // Register a new user by providing credentials. Returns the server-provided result.
    suspend fun register(username: String, email: String, password: String): RegisterResponse {
        val req = RegisterRequest(username, email, password)
        val resp = api.register(req)
        if (resp.isSuccessful) {
            return resp.body() ?: throw IOException("Empty response body for register request")
        } else {
            throw IOException("HTTP ${resp.code()} ${resp.message()}")
        }
    }
}
