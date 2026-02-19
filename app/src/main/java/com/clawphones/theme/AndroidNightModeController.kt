package com.clawphones.theme

import androidx.appcompat.app.AppCompatDelegate

/** Android-specific NightModeController implementation. */
class AndroidNightModeController : NightModeController {
    override fun apply(mode: ThemeMode) {
        val value = when (mode) {
            ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
        }
        AppCompatDelegate.setDefaultNightMode(value)
    }
}
