package com.clawphones.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test

class BatteryOptimizerTest {
    private val optimizer = BatteryOptimizer(threshold = 20)

    @Test
    fun testShouldPause_whenLowBattery_andNotCharging() {
        val battery = BatteryInfo(level = 15, isCharging = false)
        assertTrue(optimizer.shouldPauseBackgroundTasks(battery, backgroundTasksEnabled = true))
    }

    @Test
    fun testShouldPause_whenHighBattery_notCharging() {
        val battery = BatteryInfo(level = 25, isCharging = false)
        assertFalse(optimizer.shouldPauseBackgroundTasks(battery, backgroundTasksEnabled = true))
    }

    @Test
    fun testShouldPause_whenCharging() {
        val battery = BatteryInfo(level = 15, isCharging = true)
        assertFalse(optimizer.shouldPauseBackgroundTasks(battery, backgroundTasksEnabled = true))
    }

    @Test
    fun testRecommendedPauseDuration() {
        val batteryLow = BatteryInfo(level = 15, isCharging = false)
        val batteryHigh = BatteryInfo(level = 50, isCharging = false)
        assertEquals(15, optimizer.recommendedPauseDurationMinutes(batteryLow, backgroundTasksEnabled = true))
        assertEquals(0, optimizer.recommendedPauseDurationMinutes(batteryHigh, backgroundTasksEnabled = true))
    }

    @Test
    fun testShouldPause_whenBackgroundDisabled() {
        val battery = BatteryInfo(level = 10, isCharging = false)
        // When background tasks are disabled, optimizer should not pause
        assertFalse(optimizer.shouldPauseBackgroundTasks(battery, backgroundTasksEnabled = false))
    }

    @Test
    fun testShouldPause_boundaryAtThreshold_notPaused() {
        val battery = BatteryInfo(level = 20, isCharging = false)
        // At the threshold, it should not pause (exclusive check)
        assertFalse(optimizer.shouldPauseBackgroundTasks(battery, backgroundTasksEnabled = true))
    }
}
