package com.clawphones.app.analytics

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class AnalyticsManagerTest {

    @Test
    fun testEventLoggingAndRecommendationGeneration() {
        val manager = AnalyticsManager("G11-05-CP-TEST")
        manager.clear()
        manager.logEvent("login")
        manager.logEvent("browse_item", mapOf("category" to "electronics"))
        val events = manager.collectData()
        assertEquals(2, events.size)
        val recs = manager.getRecommendations()
        assertNotNull(recs)
        assertTrue(recs.isNotEmpty())
    }

    @Test
    fun testLastEventContext() {
        val manager = AnalyticsManager("G11-05-CP-TEST")
        manager.clear()
        manager.logEvent("add_to_cart", mapOf("item_id" to "ABC123"))
        val last = manager.getLastEvent()
        assertNotNull(last)
        assertEquals("add_to_cart", last!!.name)
        assertEquals("ABC123", last!!.properties["item_id"])
    }
}
