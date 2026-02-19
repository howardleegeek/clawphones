package com.clawphones.ui.settings

// Simple in-memory user preferences model for testing without Android framework
data class UserPreferences(var prefEnabled: Boolean = false)

// In-memory store representing persisted user preferences
object UserPreferencesStore {
  var current: UserPreferences = UserPreferences(false)
  fun update(enabled: Boolean) {
    current = current.copy(prefEnabled = enabled)
  }
}

// Lightweight switch abstraction to enable unit testing without Android UI.
interface ToggleSwitch {
  var isChecked: Boolean
  var onToggleListener: ((Boolean) -> Unit)?
}

// SettingsFragment exposes a Switch-like control and binds it to UserPreferences.
// In production this would be an Android Fragment with a real Switch, but for
// tests we keep it lightweight and testable.
class SettingsFragment(private val switchControl: ToggleSwitch = object : ToggleSwitch {
  override var isChecked: Boolean = false
  override var onToggleListener: ((Boolean) -> Unit)? = null
}) {
  init {
    // Initialize UI state from stored preferences
    switchControl.isChecked = UserPreferencesStore.current.prefEnabled
    // Bind changes in the UI back to the stored preferences
    switchControl.onToggleListener = { newValue ->
      UserPreferencesStore.update(newValue)
    }
  }

  // Expose the switch to tests
  fun getSwitch(): ToggleSwitch = switchControl
}
