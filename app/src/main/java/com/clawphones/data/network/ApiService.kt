package com.clawphones.data.network

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

// Requests
data class LoginRequest(val username: String, val password: String)
data class LoginResponse(val token: String, val userId: String)
data class RegisterRequest(val username: String, val password: String, val email: String)
data class RegisterResponse(val userId: String, val token: String)

// Retrofit endpoints
interface ApiEndpoints {
    @POST("login")
    suspend fun login(@Body req: LoginRequest): Response<LoginResponse>

    @POST("register")
    suspend fun register(@Body req: RegisterRequest): Response<RegisterResponse>
}

// Custom exception for API failures
class ApiException(message: String, val code: Int? = null) : Exception(message)

// Simple API client wrapper around Retrofit endpoints
class ApiClient(private val endpoints: ApiEndpoints) {
    suspend fun login(req: LoginRequest): LoginResponse {
        val resp = endpoints.login(req)
        if (resp.isSuccessful) {
            val body = resp.body()
            if (body != null) return body
            throw ApiException("Login response body is null", resp.code())
        } else {
            throw ApiException("Login failed with status ${resp.code()}", resp.code())
        }
    }

    suspend fun register(req: RegisterRequest): RegisterResponse {
        val resp = endpoints.register(req)
        if (resp.isSuccessful) {
            val body = resp.body()
            if (body != null) return body
            throw ApiException("Register response body is null", resp.code())
        } else {
            throw ApiException("Register failed with status ${resp.code()}", resp.code())
        }
    }
}

// Factory to create ApiClient with a given base URL
object ApiServiceFactory {
    fun create(baseUrl: String): ApiClient {
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val endpoints = retrofit.create(ApiEndpoints::class.java)
        return ApiClient(endpoints)
    }
}
