package com.clawphones.ui.settings

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsFragmentTest {

  @Test
  fun testInitialStateReflectsPreferences() {
    // Arrange: set initial preferences to true
    UserPreferencesStore.current = UserPreferences(true)

    // Act: create fragment
    val fragment = SettingsFragment()
    val sw = fragment.getSwitch()

    // Assert: switch reflects the stored preference
    assertTrue(sw.isChecked)
  }

  @Test
  fun testTogglingSwitchUpdatesPreferences() {
    // Arrange: start with false
    UserPreferencesStore.current = UserPreferences(false)

    val fragment = SettingsFragment()
    val sw = fragment.getSwitch()

    // Act: simulate user turning the switch ON
    sw.onToggleListener?.invoke(true)
    // Assert: preference updated
    assertTrue(UserPreferencesStore.current.prefEnabled)

    // Act: simulate user turning the switch OFF
    sw.onToggleListener?.invoke(false)
    // Assert: preference updated again
    assertFalse(UserPreferencesStore.current.prefEnabled)
  }
}
