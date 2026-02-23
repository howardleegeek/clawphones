package com.clawphones.data

import android.content.Context
import androidx.room.Room

class UserRepository(private val userDao: UserDao) {

    suspend fun saveUser(user: User) {
        userDao.insertUser(user)
    }

    suspend fun getUser(userId: String): User? {
        return userDao.getUserById(userId)
    }

    suspend fun getAllUsers(): List<User> {
        return userDao.getAllUsers()
    }

    suspend fun deleteUser(userId: String) {
        userDao.deleteUser(userId)
    }

    companion object {
        @Volatile
        private var INSTANCE: UserDatabase? = null

        fun getDatabase(context: Context): UserDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UserDatabase::class.java,
                    "user_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
