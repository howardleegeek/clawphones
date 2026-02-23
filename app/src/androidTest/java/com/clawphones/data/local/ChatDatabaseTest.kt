package com.clawphones.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class ChatDatabaseTest {
    private lateinit var db: ChatDatabase
    private lateinit var dao: ChatDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, ChatDatabase::class.java)
            .fallbackToDestructiveMigration()
            .build()
        dao = db.chatDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testInsertQueryAndSearchHistory() = runBlocking {
        val sessionId = "sess-01"
        // Insert two messages
        dao.insertMessage(ChatMessageEntity(sessionId = sessionId, sender = "user", content = "hello there", timestamp = 1))
        dao.insertMessage(ChatMessageEntity(sessionId = sessionId, sender = "bot", content = "hi! how can I help?", timestamp = 2))

        // Recent messages should return the most recent one when limit = 1
        val recent = dao.getRecentMessagesForSession(sessionId, 1)
        assertEquals(1, recent.size)
        assertEquals("hi! how can I help?", recent[0].content)

        // All messages in ascending order should contain both messages in the correct order
        val allAsc = dao.getAllMessagesForSessionAsc(sessionId)
        assertEquals(2, allAsc.size)
        assertEquals("hello there", allAsc[0].content)
        assertEquals("hi! how can I help?", allAsc[1].content)

        // Search within a session
        val search = dao.searchMessages(sessionId, "hello")
        assertEquals(1, search.size)
        assertEquals("hello there", search[0].content)
    }
}
