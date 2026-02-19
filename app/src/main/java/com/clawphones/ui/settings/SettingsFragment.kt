package com.clawphones.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import androidx.fragment.app.Fragment
import com.clawphones.R

class SettingsFragment : Fragment() {
    companion object {
        private const val PREFS_NAME = "clawphones_prefs"
        private const val NOTIFY_KEY = "notifications_enabled"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate simple layout with a single Switch for notifications
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val switch = view.findViewById<Switch>(R.id.switch_notifications)
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val enabled = prefs.getBoolean(NOTIFY_KEY, false)
        switch.isChecked = enabled
        switch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(NOTIFY_KEY, isChecked).apply()
        }
    }
}
