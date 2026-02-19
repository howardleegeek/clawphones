package com.clawphones.utils

import android.os.Bundle
import org.junit.Assert.*
import org.junit.Test

class AnalyticsTrackerTest {
    @Test
    fun testBundleFromParamsCreatesExpectedBundle() {
        val map = mapOf("key1" to "value1", "key2" to "value2")
        val bundle = AnalyticsTracker.bundleFromParams(map)
        assertEquals("value1", bundle.getString("key1"))
        assertEquals("value2", bundle.getString("key2"))
        assertEquals(2, bundle.size())
    }

    @Test
    fun testBundleFromParamsWithNulls() {
        val bundle = AnalyticsTracker.bundleFromParams(null)
        assertNotNull(bundle)
        assertEquals(0, bundle.size())
    }
}
