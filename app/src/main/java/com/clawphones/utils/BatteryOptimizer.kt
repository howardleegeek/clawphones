package com.clawphones.utils

/**
 * Battery optimization for background tasks.
 * Simple, testable logic to decide whether to pause background work based on battery state.
 * Default threshold is 20% remaining battery.
 */
data class BatteryInfo(val level: Int, val isCharging: Boolean)

class BatteryOptimizer(private val threshold: Int = 20) {
    fun shouldPauseBackgroundTasks(battery: BatteryInfo, backgroundTasksEnabled: Boolean): Boolean {
        if (!backgroundTasksEnabled) return false
        if (battery.isCharging) return false
        val lvl = battery.level.coerceIn(0, 100)
        return lvl < threshold
    }

    fun recommendedPauseDurationMinutes(battery: BatteryInfo, backgroundTasksEnabled: Boolean): Int {
        return if (shouldPauseBackgroundTasks(battery, backgroundTasksEnabled)) 15 else 0
    }
}
