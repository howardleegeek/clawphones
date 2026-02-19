package com.clawphones.app.update

import android.app.Activity

// Simple data holder for update information
data class UpdateInfo(val isAvailable: Boolean, val versionName: String? = null)

// Abstraction over the underlying in-app update SDK (e.g., Google Play Core).
interface UpdateSDK {
    fun checkForUpdate(callback: (UpdateInfo) -> Unit)
    fun startUpdateFlow(activity: Activity, updateType: Int, requestCode: Int)
}

// Public type-like constants to avoid hard dependencies on Google Play Core types in this repo.
object AppUpdateType {
    const val IMMEDIATE = 1
    const val FLEXIBLE = 2
}

/**
 * Manages in-app update flow with a small, test-friendly surface.
 * - By default, it uses a no-op UpdateSDK to avoid tight coupling during tests.
 * - Tests can inject a fake UpdateSDK to simulate various update scenarios.
 */
class UpdateManager(
    private val activity: Activity,
    private val updateSDKProvider: (() -> UpdateSDK)? = null
) {

    val UPDATE_REQUEST_CODE = 1001

    // UI consumer can subscribe to this to show a prompt to the user.
    var onUpdatePrompt: ((UpdateInfo) -> Unit)? = null

    private val updateSDK: UpdateSDK by lazy {
        updateSDKProvider?.invoke() ?: object : UpdateSDK {
            override fun checkForUpdate(callback: (UpdateInfo) -> Unit) {
                // Default no-op: no update available
                callback(UpdateInfo(false))
            }

            override fun startUpdateFlow(activity: Activity, updateType: Int, requestCode: Int) {
                // No-op in default implementation
            }
        }
    }

    /** Check for updates and notify UI through onUpdatePrompt if an update is available. */
    fun checkForUpdate() {
        updateSDK.checkForUpdate { info ->
            if (info.isAvailable) {
                onUpdatePrompt?.invoke(info)
            }
        }
    }

    /** Start the update flow using the provided SDK. */
    fun startUpdateFlow(updateType: Int = AppUpdateType.IMMEDIATE) {
        updateSDK.startUpdateFlow(activity, updateType, UPDATE_REQUEST_CODE)
    }
}
