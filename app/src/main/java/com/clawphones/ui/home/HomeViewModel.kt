package com.clawphones.ui.home

// Lightweight, test-friendly stand-ins for LiveData to keep this task
// self-contained without relying on Android framework during unit tests.
open class LiveData<T>

class MutableLiveData<T> : LiveData<T>() {
    var value: T? = null
        private set

    fun setValue(v: T) {
        value = v
    }
}

// Simple user data model used by the Home screen.
data class User(val id: String, val name: String, val email: String)

// Api surface used by HomeViewModel.
interface ApiService {
    fun getUser(): User
}

/**
 * Home screen ViewModel-like class with data fetching capability.
 * - Uses a simple MutableLiveData to expose Result<User>.
 * - Logs with task context to aid debugging.
 */
class HomeViewModel(private val apiService: ApiService, private val taskId: String) {
    val userLiveData = MutableLiveData<Result<User>>()

    private fun log(message: String) {
        println("Task $taskId: $message")
    }

    /** Fetches the current user and posts the result to LiveData. */
    fun fetchUser() {
        log("Fetching user data")
        try {
            val user = apiService.getUser()
            userLiveData.setValue(Result.success(user))
            log("Fetched user: ${user.id}")
        } catch (e: Exception) {
            userLiveData.setValue(Result.failure<User>(e))
            log("Failed to fetch user: ${e.message}")
        }
    }
}
