package com.clawphones.ui.home

import org.junit.Assert.*
import org.junit.Test

class HomeViewModelTest {

    // Successful API
    class FakeOkApi : ApiService {
        override fun getUser(): User = User("1", "Alice", "alice@example.com")
    }

    // Failing API
    class FakeFailApi : ApiService {
        override fun getUser(): User {
            throw RuntimeException("network error")
        }
    }

    @Test
    fun testFetchUserSuccess() {
        val vm = HomeViewModel(FakeOkApi(), "TASK-SUCC-1")
        vm.fetchUser()
        val result = vm.userLiveData.value
        assertNotNull("Result should not be null", result)
        assertTrue("Result should be success", result!!.isSuccess)
        val user = result.getOrNull()
        assertNotNull("User should not be null", user)
        assertEquals("Alice", user!!.name)
    }

    @Test
    fun testFetchUserFailure() {
        val vm = HomeViewModel(FakeFailApi(), "TASK_FAIL-1")
        vm.fetchUser()
        val result = vm.userLiveData.value
        assertNotNull("Result should not be null", result)
        assertTrue("Result should be failure", result!!.isFailure)
    }
}
