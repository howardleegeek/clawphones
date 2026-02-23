package com.clawphones.data.local

import android.content.Context
import androidx.room.*

// Entity representing a single chat message in a session
@Entity(
    tableName = "chat_messages",
    indices = [Index(value = ["sessionId"]), Index(value = ["timestamp"])]
)
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String,
    val sender: String,
    val content: String,
    val timestamp: Long
)

// DAO for chat history persistence
@Dao
interface ChatDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    // Load most recent messages for a given session
    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentMessagesForSession(sessionId: String, limit: Int): List<ChatMessageEntity>

    // Search messages within a session by content (case-insensitive LIKE)
    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId AND content LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    suspend fun searchMessages(sessionId: String, query: String): List<ChatMessageEntity>

    // Get all messages for a session in ascending order (oldest first)
    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getAllMessagesForSessionAsc(sessionId: String): List<ChatMessageEntity>

    // Clear history for a session (useful for tests or reset)
    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun clearMessagesForSession(sessionId: String)
}

@Database(entities = [ChatMessageEntity::class], version = 1, exportSchema = false)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: ChatDatabase? = null

        fun getInstance(context: Context): ChatDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChatDatabase::class.java,
                    "chat_history.db"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
