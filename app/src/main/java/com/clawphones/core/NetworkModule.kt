package com.clawphones.core

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException

// Simple data model for demonstration
data class Data(val id: Int, val content: String)

// Abstraction of a network API service. In real project this would be a Retrofit interface.
interface ApiService {
    suspend fun getData(): Data
}

// Network module using coroutines and Flow to fetch data asynchronously with retry on IO errors.
class NetworkModule(private val api: ApiService) {
    // Expose a Flow that emits a single Data item, retrying on IOExceptions up to a limit.
    fun fetchDataFlow(): Flow<Data> = flow {
        val maxAttempts = 3
        var attempts = 0
        var lastError: Throwable? = null
        while (attempts < maxAttempts) {
            try {
                val data = api.getData()
                emit(data)
                return@flow
            } catch (e: IOException) {
                lastError = e
                attempts++
                if (attempts < maxAttempts) {
                    // small backoff before retry
                    delay(100)
                }
            }
        }
        // All attempts exhausted
        throw (lastError ?: IOException("Unknown network error"))
    }
}
