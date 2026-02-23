package com.clawphones.utils

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

// Analytics tracker integrating Firebase Analytics for event tracking
class AnalyticsTracker private constructor(private val firebaseAnalytics: FirebaseAnalytics) {

    companion object {
        @Volatile
        private var INSTANCE: AnalyticsTracker? = null

        // Obtain a singleton instance bound to application/context
        fun getInstance(context: Context): AnalyticsTracker =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: AnalyticsTracker(FirebaseAnalytics.getInstance(context)).also { INSTANCE = it }
            }

        // Testing helper: create a Bundle from a map of parameters (no Firebase dependency)
        @JvmStatic
        internal fun bundleFromParams(params: Map<String, String>?): Bundle {
            val bundle = Bundle()
            params?.forEach { (k, v) -> bundle.putString(k, v) }
            return bundle
        }

        // Testing hook: allow injecting a fake FirebaseAnalytics instance for unit tests
        internal fun createForTesting(fakeAnalytics: FirebaseAnalytics): AnalyticsTracker =
            AnalyticsTracker(fakeAnalytics)
    }

    // Generic event logger
    fun logEvent(eventName: String, params: Map<String, String>? = null) {
        val bundle = bundleFromParams(params)
        firebaseAnalytics.logEvent(eventName, bundle)
    }

    // Convenience: track a generic button click
    fun logButtonClick(buttonName: String) {
        val bundle = Bundle().apply { putString("button", buttonName) }
        firebaseAnalytics.logEvent("button_click", bundle)
    }

    // Convenience: track a screen view
    fun logScreenView(screenName: String) {
        val bundle = Bundle().apply {
            putString(com.google.firebase.analytics.FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        }
        firebaseAnalytics.logEvent(com.google.firebase.analytics.FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }
}
