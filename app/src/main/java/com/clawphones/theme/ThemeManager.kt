package com.clawphones.theme

/**
 * Central theme controller for Clawphones.
 * - Persists user preference for LIGHT / DARK / SYSTEM
 * - Applies the corresponding night mode using a pluggable controller
 * - Keeps logic testable without Android framework dependencies
 */
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

interface ThemePrefs {
    fun getMode(): ThemeMode
    fun setMode(mode: ThemeMode)
}

interface NightModeController {
    fun apply(mode: ThemeMode)
}

class ThemeManager(private val prefs: ThemePrefs, private val controller: NightModeController) {
    fun applyTheme() {
        controller.apply(prefs.getMode())
    }

    fun setMode(mode: ThemeMode) {
        prefs.setMode(mode)
        applyTheme()
    }
}
