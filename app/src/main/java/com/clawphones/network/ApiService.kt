package com.clawphones.network

import retrofit2.http.Body
import retrofit2.http.POST

data class LoginRequest(
    val username: String,
    val password: String
)

data class UserInfo(
    val id: String,
    val username: String,
    val email: String,
    val token: String
)

data class LoginResponse(
    val success: Boolean,
    val message: String?,
    val user: UserInfo?
)

interface ApiService {
    @POST("/api/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
}
