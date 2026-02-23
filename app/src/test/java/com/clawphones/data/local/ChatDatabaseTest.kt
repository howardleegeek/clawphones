package com.clawphones.data.local

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ChatDatabaseTest {
    private lateinit var db: ChatDatabase
    private lateinit var dao: ChatDao

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        db = ChatDatabase.getInstance(context)
        dao = db.chatDao()
    }

    @After
    fun tearDown() {
        // Clean up test data to isolate tests; avoid closing the shared instance to keep setup simple.
        runBlocking {
            dao.clearMessagesForSession("sess_for_test")
            dao.clearMessagesForSession("sess_new")
        }
    }

    @Test
    fun insertAndQueryAllForSession() = runBlocking {
        val ts = System.currentTimeMillis()
        val msg = ChatMessageEntity(sessionId = "sess_for_test", sender = "user", content = "hello", timestamp = ts)
        dao.insertMessage(msg)
        val results = dao.getAllMessagesForSessionAsc("sess_for_test")
        assertEquals(1, results.size)
        assertEquals("hello", results[0].content)
    }

    @Test
    fun searchMessagesWithinSession() = runBlocking {
        dao.insertMessage(ChatMessageEntity(sessionId = "sess_for_test", sender = "user", content = "hello world", timestamp = System.currentTimeMillis()))
        dao.insertMessage(ChatMessageEntity(sessionId = "sess_for_test", sender = "bot", content = "hi there", timestamp = System.currentTimeMillis()))
        val res = dao.searchMessages("sess_for_test", "hello")
        assertEquals(1, res.size)
        assertEquals("hello world", res[0].content)
    }

    @Test
    fun loadRecentHistoryForNewSession() = runBlocking {
        // simulate existing history for a new session id
        dao.insertMessage(ChatMessageEntity(sessionId = "sess_new", sender = "user", content = "first", timestamp = 1000L))
        dao.insertMessage(ChatMessageEntity(sessionId = "sess_new", sender = "bot", content = "second", timestamp = 2000L))
        val recent = dao.getRecentMessagesForSession("sess_new", 2)
        assertEquals(2, recent.size)
        // latest should be the one with higher timestamp
        assertEquals("second", recent[0].content)
    }
}
