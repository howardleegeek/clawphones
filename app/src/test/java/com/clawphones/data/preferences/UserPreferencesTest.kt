package com.clawphones.data.preferences

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import android.content.SharedPreferences

/**
 * Unit tests for UserPreferences storage using a fake SharedPreferences.
 * Note: This test avoids Android dependencies by using an in-memory stub.
 */
class UserPreferencesTest {

  @Test
  fun testSaveAndLoadRoundTrip() {
    val prefs = FakeSharedPreferences()
    val store = UserPreferencesStore(prefs)
    val original = UserPreferences("dark", false, 18)
    store.save(original)
    val loaded = store.load()
    assertEquals(original, loaded)
  }

  @Test
  fun testDefaultsWhenNoData() {
    val prefs = FakeSharedPreferences()
    val store = UserPreferencesStore(prefs)
    val loaded = store.load()
    assertEquals("light", loaded.theme)
    assertEquals(true, loaded.notificationsEnabled)
    assertEquals(14, loaded.fontSize)
  }
}

/** Minimal in-memory fake SharedPreferences for unit testing */
private class FakeSharedPreferences : SharedPreferences {
  private val map = mutableMapOf<String, Any?>()
  private val listeners = mutableSetOf<SharedPreferences.OnSharedPreferenceChangeListener>()

  override fun getAll(): MutableMap<String, *> = map
  override fun getString(key: String, defValue: String?): String? = map[key] as String? ?: defValue
  override fun getStringSet(key: String, defValues: MutableSet<String>?): MutableSet<String>? {
    @Suppress("UNCHECKED_CAST")
    return (map[key] as? MutableSet<String>) ?: defValues
  }
  override fun getBoolean(key: String, defValue: Boolean): Boolean = map[key] as? Boolean ?: defValue
  override fun getInt(key: String, defValue: Int): Int = map[key] as? Int ?: defValue
  override fun getLong(key: String, defValue: Long): Long = map[key] as? Long ?: defValue
  override fun getFloat(key: String, defValue: Float): Float = map[key] as? Float ?: defValue
  override fun contains(key: String): Boolean = map.containsKey(key)
  override fun edit(): SharedPreferences.Editor = FakeEditor()
  override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
    listeners.add(listener)
  }
  override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
    listeners.remove(listener)
  }

  private inner class FakeEditor : SharedPreferences.Editor {
    private val tmp = mutableMapOf<String, Any?>()

    override fun putString(key: String, value: String?): SharedPreferences.Editor {
      tmp[key] = value
      return this
    }
    override fun putStringSet(key: String, values: MutableSet<String>?): SharedPreferences.Editor {
      tmp[key] = values
      return this
    }
    override fun putInt(key: String, value: Int): SharedPreferences.Editor {
      tmp[key] = value
      return this
    }
    override fun putLong(key: String, value: Long): SharedPreferences.Editor {
      tmp[key] = value
      return this
    }
    override fun putFloat(key: String, value: Float): SharedPreferences.Editor {
      tmp[key] = value
      return this
    }
    override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor {
      tmp[key] = value
      return this
    }
    override fun remove(key: String): SharedPreferences.Editor {
      tmp[key] = null
      return this
    }
    override fun clear(): SharedPreferences.Editor {
      tmp.clear()
      return this
    }
    override fun commit(): Boolean {
      for ((k, v) in tmp) {
        if (v == null) map.remove(k) else map[k] = v
      }
      tmp.clear()
      // In this fake, always succeed
      return true
    }
    override fun apply() {
      commit()
    }
  }
}
