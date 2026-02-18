package com.clawphones.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserRepositoryTest {

    private lateinit var userDao: UserDao
    private lateinit var userRepository: UserRepository

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, UserDatabase::class.java).build()
        userDao = database.userDao()
        userRepository = UserRepository(userDao)
    }

    @Test
    fun saveUser_shouldPersistUser() = runBlocking {
        val user = User(
            id = "user1",
            name = "John Doe",
            email = "john@example.com"
        )

        userRepository.saveUser(user)
        val retrievedUser = userRepository.getUser("user1")

        assertNotNull(retrievedUser)
        assertEquals("user1", retrievedUser?.id)
        assertEquals("John Doe", retrievedUser?.name)
        assertEquals("john@example.com", retrievedUser?.email)
    }

    @Test
    fun getUser_shouldReturnNullForNonExistentUser() = runBlocking {
        val user = userRepository.getUser("nonexistent")
        assertEquals(null, user)
    }

    @Test
    fun getAllUsers_shouldReturnAllSavedUsers() = runBlocking {
        val user1 = User(id = "user1", name = "John Doe", email = "john@example.com")
        val user2 = User(id = "user2", name = "Jane Doe", email = "jane@example.com")

        userRepository.saveUser(user1)
        userRepository.saveUser(user2)

        val allUsers = userRepository.getAllUsers()

        assertEquals(2, allUsers.size)
    }

    @Test
    fun deleteUser_shouldRemoveUser() = runBlocking {
        val user = User(id = "user1", name = "John Doe", email = "john@example.com")
        userRepository.saveUser(user)

        userRepository.deleteUser("user1")
        val retrievedUser = userRepository.getUser("user1")

        assertEquals(null, retrievedUser)
    }

    @Test
    fun saveUser_shouldUpdateExistingUser() = runBlocking {
        val originalUser = User(id = "user1", name = "John Doe", email = "john@example.com")
        userRepository.saveUser(originalUser)

        val updatedUser = User(id = "user1", name = "John Smith", email = "johnsmith@example.com")
        userRepository.saveUser(updatedUser)

        val retrievedUser = userRepository.getUser("user1")

        assertEquals("John Smith", retrievedUser?.name)
        assertEquals("johnsmith@example.com", retrievedUser?.email)
    }
}
