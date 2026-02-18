package com.example.clawphones

import android.os.StrictMode
import java.net.URL
import javax.net.ssl.HttpsURLConnection

// Minimal HTTPS config fetcher for Android client.
data class DeviceConfig(val apiURL: String)

object DeviceConfigFetcher {
    // This is a very lightweight fetcher suitable for tests; production should use async APIs and proper TLS config.
    fun fetch(baseURL: String): DeviceConfig {
        val url = URL(baseURL.trimEnd('/') + "/config.json")
        val conn = url.openConnection() as HttpsURLConnection
        // Permit network on main thread for demonstration; in real app use async.
        val policy = StrictMode.ThreadPolicy.LAX
        StrictMode.setThreadPolicy(policy)
        try {
            conn.connect()
            val json = conn.inputStream.bufferedReader().use { it.readText() }
            // Very small parser; assume JSON: {"apiURL": "https://..."}
            val apiURL = Regex("\"apiURL\"\s*:\s*\"([^\"]+)\"").find(json)?.groupValues?.get(1) ?: ""
            return DeviceConfig(apiURL)
        } finally {
            conn.disconnect()
        }
    }
}
