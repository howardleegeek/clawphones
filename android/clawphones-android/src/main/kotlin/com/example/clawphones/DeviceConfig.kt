package com.example.clawphones

import android.os.StrictMode
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import java.security.cert.X509Certificate

data class DeviceConfig(val apiURL: String)

object DeviceConfigFetcher {
    private fun createTrustAllContext(): SSLContext {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        })
        val context = SSLContext.getInstance("TLS")
        context.init(null, trustAllCerts, java.security.SecureRandom())
        return context
    }

    fun fetch(baseURL: String): DeviceConfig {
        val url = URL(baseURL.trimEnd('/') + "/config.json")
        val conn = url.openConnection() as HttpsURLConnection
        conn.sslSocketFactory = createTrustAllContext().socketFactory
        conn.hostnameVerifier = javax.net.ssl.HostnameVerifier { _, _ -> true }
        val policy = StrictMode.ThreadPolicy.LAX
        StrictMode.setThreadPolicy(policy)
        try {
            conn.connect()
            val json = conn.inputStream.bufferedReader().use { it.readText() }
            val apiURL = Regex("\"apiURL\"\s*:\s*\"([^\"]+)\"").find(json)?.groupValues?.get(1) ?: ""
            return DeviceConfig(apiURL)
        } finally {
            conn.disconnect()
        }
    }
}
