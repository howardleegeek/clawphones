package com.clawphones.ui.settings

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SettingsFragmentTest {

  @Before
  fun setup() {
    UserPreferencesStore.current = UserPreferences()
  }

  @Test
  fun testInitialStateReflectsPreferences() {
    UserPreferencesStore.current = UserPreferences(prefEnabled = true)

    val fragment = SettingsFragment()
    val sw = fragment.getPrefSwitch()

    assertTrue(sw.isChecked)
  }

  @Test
  fun testTogglingSwitchUpdatesPreferences() {
    UserPreferencesStore.current = UserPreferences(prefEnabled = false)

    val fragment = SettingsFragment()
    val sw = fragment.getPrefSwitch()

    sw.onToggleListener?.invoke(true)
    assertTrue(UserPreferencesStore.current.prefEnabled)

    sw.onToggleListener?.invoke(false)
    assertFalse(UserPreferencesStore.current.prefEnabled)
  }

  @Test
  fun testNotificationsSwitchInitialStateReflectsPreferences() {
    UserPreferencesStore.current = UserPreferences(notificationsEnabled = true)

    val fragment = SettingsFragment()
    val sw = fragment.getNotificationsSwitch()

    assertTrue(sw.isChecked)
  }

  @Test
  fun testTogglingNotificationsSwitchUpdatesPreferences() {
    UserPreferencesStore.current = UserPreferences(notificationsEnabled = false)

    val fragment = SettingsFragment()
    val sw = fragment.getNotificationsSwitch()

    sw.onToggleListener?.invoke(true)
    assertTrue(UserPreferencesStore.current.notificationsEnabled)

    sw.onToggleListener?.invoke(false)
    assertFalse(UserPreferencesStore.current.notificationsEnabled)
  }

  @Test
  fun testDarkModeSwitchInitialStateReflectsPreferences() {
    UserPreferencesStore.current = UserPreferences(darkModeEnabled = true)

    val fragment = SettingsFragment()
    val sw = fragment.getDarkModeSwitch()

    assertTrue(sw.isChecked)
  }

  @Test
  fun testTogglingDarkModeSwitchUpdatesPreferences() {
    UserPreferencesStore.current = UserPreferences(darkModeEnabled = false)

    val fragment = SettingsFragment()
    val sw = fragment.getDarkModeSwitch()

    sw.onToggleListener?.invoke(true)
    assertTrue(UserPreferencesStore.current.darkModeEnabled)

    sw.onToggleListener?.invoke(false)
    assertFalse(UserPreferencesStore.current.darkModeEnabled)
  }

  @Test
  fun testAllSwitchesIndependent() {
    UserPreferencesStore.current = UserPreferences(
      prefEnabled = false,
      notificationsEnabled = true,
      darkModeEnabled = false
    )

    val fragment = SettingsFragment()
    val prefSw = fragment.getPrefSwitch()
    val notifSw = fragment.getNotificationsSwitch()
    val darkSw = fragment.getDarkModeSwitch()

    assertFalse(prefSw.isChecked)
    assertTrue(notifSw.isChecked)
    assertFalse(darkSw.isChecked)

    prefSw.onToggleListener?.invoke(true)
    darkSw.onToggleListener?.invoke(true)

    assertTrue(UserPreferencesStore.current.prefEnabled)
    assertTrue(UserPreferencesStore.current.notificationsEnabled)
    assertTrue(UserPreferencesStore.current.darkModeEnabled)
  }
}
