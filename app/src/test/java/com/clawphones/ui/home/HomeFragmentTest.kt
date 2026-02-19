package com.clawphones.ui.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.clawphones.data.model.User
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class HomeFragmentTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        viewModel = HomeViewModel()
    }

    @Test
    fun `initial user data is loaded correctly`() {
        val user = viewModel.user.value
        
        assertEquals("1", user?.id)
        assertEquals("John Doe", user?.name)
        assertEquals("john.doe@example.com", user?.email)
        assertEquals("+1-555-1234", user?.phone)
    }

    @Test
    fun `updateUser updates user data`() {
        val newUser = User(
            id = "2",
            name = "Jane Smith",
            email = "jane.smith@example.com",
            phone = "+1-555-5678"
        )
        
        viewModel.updateUser(newUser)
        
        val user = viewModel.user.value
        assertEquals("2", user?.id)
        assertEquals("Jane Smith", user?.name)
        assertEquals("jane.smith@example.com", user?.email)
        assertEquals("+1-555-5678", user?.phone)
    }

    @Test
    fun `user LiveData updates correctly`() {
        var observedUser: User? = null
        viewModel.user.observeForever { user ->
            observedUser = user
        }
        
        val newUser = User(
            id = "3",
            name = "Bob Wilson",
            email = "bob.wilson@example.com",
            phone = "+1-555-9999"
        )
        
        viewModel.updateUser(newUser)
        
        assertEquals("Bob Wilson", observedUser?.name)
        assertEquals("bob.wilson@example.com", observedUser?.email)
        assertEquals("+1-555-9999", observedUser?.phone)
    }
}
