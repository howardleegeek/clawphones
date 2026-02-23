package com.clawphones.theme

import org.junit.Assert.assertEquals
import org.junit.Test

class ThemeManagerTest {
    private class FakePrefs(initial: ThemeMode) : ThemePrefs {
        private var mode: ThemeMode = initial
        override fun getMode(): ThemeMode = mode
        override fun setMode(mode: ThemeMode) { this.mode = mode }
    }

    private class SpyController : NightModeController {
        var lastApplied: ThemeMode? = null
        override fun apply(mode: ThemeMode) { lastApplied = mode }
    }

    @Test
    fun testSetModePersistsAndApplies() {
        val prefs = FakePrefs(ThemeMode.SYSTEM)
        val ctrl = SpyController()
        val manager = ThemeManager(prefs, ctrl)

        manager.setMode(ThemeMode.DARK)

        // persistence should reflect the new mode
        assertEquals(ThemeMode.DARK, prefs.getMode())
        // controller should have been invoked with the new mode
        assertEquals(ThemeMode.DARK, ctrl.lastApplied)
    }

    @Test
    fun testApplyThemeUsesCurrentPrefs() {
        val prefs = FakePrefs(ThemeMode.LIGHT)
        val ctrl = SpyController()
        val manager = ThemeManager(prefs, ctrl)

        manager.applyTheme()

        assertEquals(ThemeMode.LIGHT, ctrl.lastApplied)
    }
}
