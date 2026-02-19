package com.clawphones.ui.settings

data class UserPreferences(
  var prefEnabled: Boolean = false,
  var notificationsEnabled: Boolean = true,
  var darkModeEnabled: Boolean = false
)

object UserPreferencesStore {
  var current: UserPreferences = UserPreferences()
  
  fun update(enabled: Boolean) {
    current = current.copy(prefEnabled = enabled)
  }
  
  fun updateNotifications(enabled: Boolean) {
    current = current.copy(notificationsEnabled = enabled)
  }
  
  fun updateDarkMode(enabled: Boolean) {
    current = current.copy(darkModeEnabled = enabled)
  }
}

interface ToggleSwitch {
  var isChecked: Boolean
  var onToggleListener: ((Boolean) -> Unit)?
}

class SettingsFragment(
  private val prefSwitch: ToggleSwitch = object : ToggleSwitch {
    override var isChecked: Boolean = false
    override var onToggleListener: ((Boolean) -> Unit)? = null
  },
  private val notificationsSwitch: ToggleSwitch = object : ToggleSwitch {
    override var isChecked: Boolean = false
    override var onToggleListener: ((Boolean) -> Unit)? = null
  },
  private val darkModeSwitch: ToggleSwitch = object : ToggleSwitch {
    override var isChecked: Boolean = false
    override var onToggleListener: ((Boolean) -> Unit)? = null
  }
  ) {
  // Secondary constructor for testing: allow injecting custom switch mocks
  constructor(
    prefSwitch: ToggleSwitch,
    notificationsSwitch: ToggleSwitch,
    darkModeSwitch: ToggleSwitch
  ) : this(prefSwitch, notificationsSwitch, darkModeSwitch)
  init {
    prefSwitch.isChecked = UserPreferencesStore.current.prefEnabled
    prefSwitch.onToggleListener = { newValue ->
      UserPreferencesStore.update(newValue)
    }
    
    notificationsSwitch.isChecked = UserPreferencesStore.current.notificationsEnabled
    notificationsSwitch.onToggleListener = { newValue ->
      UserPreferencesStore.updateNotifications(newValue)
    }
    
    darkModeSwitch.isChecked = UserPreferencesStore.current.darkModeEnabled
    darkModeSwitch.onToggleListener = { newValue ->
      UserPreferencesStore.updateDarkMode(newValue)
    }
  }

  fun getPrefSwitch(): ToggleSwitch = prefSwitch
  fun getNotificationsSwitch(): ToggleSwitch = notificationsSwitch
  fun getDarkModeSwitch(): ToggleSwitch = darkModeSwitch
}
