package com.clawphones.data.preferences

/**
 * Data class representing user preferences stored in SharedPreferences.
 */
data class UserPreferences(
    val theme: String,
    val notificationsEnabled: Boolean,
    val fontSize: Int
)

/**
 * Storage helper for UserPreferences using Android's SharedPreferences.
 * Stores each field as a separate key for simplicity and testability.
 */
class UserPreferencesStore(private val prefs: android.content.SharedPreferences) {
  fun save(value: UserPreferences) {
    prefs.edit()
      .putString(KEY_THEME, value.theme)
      .putBoolean(KEY_NOTIF, value.notificationsEnabled)
      .putInt(KEY_FONT_SIZE, value.fontSize)
      .apply()
  }

  fun load(): UserPreferences {
    val theme = prefs.getString(KEY_THEME, "light") ?: "light"
    val notif = prefs.getBoolean(KEY_NOTIF, true)
    val font = prefs.getInt(KEY_FONT_SIZE, 14)
    return UserPreferences(theme, notif, font)
  }

  companion object {
    private const val KEY_THEME = "user_theme"
    private const val KEY_NOTIF = "user_notifications"
    private const val KEY_FONT_SIZE = "user_font_size"
  }
}
