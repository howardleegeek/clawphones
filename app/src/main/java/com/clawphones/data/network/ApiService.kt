package com.clawphones.data.network

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import kotlin.text.Regex

// Simple data models for user update
data class User(val id: String, val name: String, val email: String, val phone: String)

data class UserUpdateRequest(val name: String? = null, val email: String? = null, val phone: String? = null)

data class UserUpdateResponse(val success: Boolean, val user: User?)

// API contract (minimal, no Retrofit dependency)
interface ApiService {
    fun updateUser(userId: String, update: UserUpdateRequest): UserUpdateResponse
}

// Public exception type for network failures
class NetworkException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

/**
 * Lightweight HTTP implementation of ApiService.
 * Assumes baseUrl ends with '/'.
 */
class HttpApiService(private val baseUrl: String) : ApiService {
    override fun updateUser(userId: String, update: UserUpdateRequest): UserUpdateResponse {
        val url = URL("${baseUrl}users/$userId")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "PUT"
        conn.doOutput = true
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
        val body = buildJson(update)
        try {
            val bytes = body.toByteArray(StandardCharsets.UTF_8)
            conn.setFixedLengthStreamingMode(bytes.size)
            conn.outputStream.use { os -> os.write(bytes) }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val resp = stream?.bufferedReader(StandardCharsets.UTF_8).use { it?.readText() ?: "" }
            if (code !in 200..299) {
                throw NetworkException("Request failed with code $code: $resp")
            }
            return parseResponse(resp ?: "")
        } catch (e: Exception) {
            throw NetworkException("Failed to update user", e)
        } finally {
            conn.disconnect()
        }
    }

    private fun buildJson(update: UserUpdateRequest): String {
        val parts = mutableListOf<String>()
        update.name?.let { parts.add("\"name\":\"${escape(it)}\"") }
        update.email?.let { parts.add("\"email\":\"${escape(it)}\"") }
        update.phone?.let { parts.add("\"phone\":\"${escape(it)}\"") }
        return "{${parts.joinToString(",")}}"
    }

    private fun escape(s: String): String = s.replace("\"", "\\\"")

    private fun parseResponse(resp: String): UserUpdateResponse {
        val success = resp.contains("\"success\":true")
        var user: User? = null
        val userMatch = Regex("\"user\"\s*:\s*\{([^}]*)\}").find(resp)
        userMatch?.let { m ->
            val body = m.groupValues[1]
            val id = Regex("\"id\"\s*:\s*\"([^\"]+)\"").find(body)?.groupValues?.get(1) ?: ""
            val name = Regex("\"name\"\s*:\s*\"([^\"]+)\"").find(body)?.groupValues?.get(1) ?: ""
            val email = Regex("\"email\"\s*:\s*\"([^\"]+)\"").find(body)?.groupValues?.get(1) ?: ""
            val phone = Regex("\"phone\"\s*:\s*\"([^\"]+)\"").find(body)?.groupValues?.get(1) ?: ""
            user = User(id, name, email, phone)
        }
        return UserUpdateResponse(success, user)
    }
}
