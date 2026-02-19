package com.clawphones.ui.settings

import android.content.Context
import com.clawphones.R
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class SettingsFragmentTest {

    private fun createActivity(): AppCompatActivity {
        return Robolectric.buildActivity(AppCompatActivity::class.java)
            .create().start().resume().get()
    }

    @Test
    fun testNotificationTogglePersistsInPrefs() {
        val activity = createActivity()
        val fragment = SettingsFragment()
        activity.supportFragmentManager.beginTransaction().add(fragment, "settings").commitNow()

        val root = fragment.requireView()
        val switch = root.findViewById<Switch>(R.id.switch_notifications)
        assertNotNull(switch)

        val prefs = activity.getSharedPreferences("clawphones_prefs", Context.MODE_PRIVATE)
        // default should be false
        assertFalse(prefs.getBoolean("notifications_enabled", false))

        // simulate user enabling notifications
        // start from unchecked state
        if (switch.isChecked) {
            switch.isChecked = false
        }
        // perform click to toggle on
        switch.performClick()

        // value should be saved
        assertTrue(prefs.getBoolean("notifications_enabled", false))
    }

    @Test
    fun testPreferenceRestoredOnRecreate() {
        val activity1 = createActivity()
        val fragment1 = SettingsFragment()
        activity1.supportFragmentManager.beginTransaction().add(fragment1, "settings").commitNow()
        val root1 = fragment1.requireView()
        val switch1 = root1.findViewById<Switch>(R.id.switch_notifications)

        // Ensure it's false -> turn on
        if (!switch1.isChecked) {
            switch1.performClick()
        }
        val prefs = activity1.getSharedPreferences("clawphones_prefs", Context.MODE_PRIVATE)
        assertTrue(prefs.getBoolean("notifications_enabled", false))

        // simulate app restart by creating a new activity and fragment
        val activity2 = createActivity()
        val fragment2 = SettingsFragment()
        activity2.supportFragmentManager.beginTransaction().add(fragment2, "settings2").commitNow()
        val root2 = fragment2.requireView()
        val switch2 = root2.findViewById<Switch>(R.id.switch_notifications)
        // The switch should reflect saved preference
        assertTrue(switch2.isChecked)
    }
}
