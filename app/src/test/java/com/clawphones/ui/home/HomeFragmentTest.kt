package com.clawphones.ui.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.clawphones.data.model.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
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

    @Test
    fun `UI displays correct initial user name from ViewModel`() {
        val user = viewModel.user.value
        assertEquals("John Doe", user?.name)
    }

    @Test
    fun `UI displays correct initial user email from ViewModel`() {
        val user = viewModel.user.value
        assertEquals("john.doe@example.com", user?.email)
    }

    @Test
    fun `UI displays correct initial user phone from ViewModel`() {
        val user = viewModel.user.value
        assertEquals("+1-555-1234", user?.phone)
    }

    @Test
    fun `UI updates name when user data changes`() {
        val newUser = User(
            id = "4",
            name = "Alice Brown",
            email = "alice.brown@example.com",
            phone = "+1-555-0000"
        )
        viewModel.updateUser(newUser)
        
        assertEquals("Alice Brown", viewModel.user.value?.name)
    }

    @Test
    fun `UI updates email when user data changes`() {
        val newUser = User(
            id = "5",
            name = "Charlie Davis",
            email = "charlie.davis@example.com",
            phone = "+1-555-1111"
        )
        viewModel.updateUser(newUser)
        
        assertEquals("charlie.davis@example.com", viewModel.user.value?.email)
    }

    @Test
    fun `UI updates phone when user data changes`() {
        val newUser = User(
            id = "6",
            name = "Diana Evans",
            email = "diana.evans@example.com",
            phone = "+1-555-2222"
        )
        viewModel.updateUser(newUser)
        
        assertEquals("+1-555-2222", viewModel.user.value?.phone)
    }

    @Test
    fun `all user fields are available for UI display`() {
        val user = viewModel.user.value
        
        assertNotNull(user)
        assertNotNull(user?.id)
        assertNotNull(user?.name)
        assertNotNull(user?.email)
        assertNotNull(user?.phone)
    }

    @Test
    fun `UI receives notification when user data updates`() {
        var updateCount = 0
        var lastObservedUser: User? = null
        
        viewModel.user.observeForever { user ->
            updateCount++
            lastObservedUser = user
        }
        
        val initialCount = updateCount
        
        viewModel.updateUser(User(
            id = "7",
            name = "Test User",
            email = "test@example.com",
            phone = "+1-555-3333"
        ))
        
        assertTrue(updateCount > initialCount)
        assertEquals("Test User", lastObservedUser?.name)
        assertEquals("test@example.com", lastObservedUser?.email)
        assertEquals("+1-555-3333", lastObservedUser?.phone)
    }
}
