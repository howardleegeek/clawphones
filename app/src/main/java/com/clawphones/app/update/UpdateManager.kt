package com.clawphones.app.update

import android.app.Activity
import android.content.Intent
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.tasks.OnSuccessListener
import com.google.android.play.core.tasks.Task

/**
 * Centralized manager for triggering In-App Updates using the Play Core library.
 * This class is intentionally lightweight and non-invasive to existing UI.
 */
class UpdateManager @JvmOverloads constructor(
    private val activity: Activity,
    private val injectedAppUpdateManager: AppUpdateManager? = null
) {

    private val UPDATE_REQUEST_CODE = 1001
    private val appUpdateManager: AppUpdateManager = injectedAppUpdateManager ?:
        AppUpdateManagerFactory.create(activity)

    /** Check for available updates and start the update flow if allowed. */
    fun checkForUpdate() {
        val infoTask: Task<AppUpdateInfo> = appUpdateManager.appUpdateInfo
        infoTask.addOnSuccessListener(object : OnSuccessListener<AppUpdateInfo> {
            override fun onSuccess(appUpdateInfo: AppUpdateInfo) {
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
                ) {
                    // Immediately start the update flow for a seamless experience
                    appUpdateManager.startUpdateFlow(
                        appUpdateInfo,
                        AppUpdateType.IMMEDIATE,
                        activity,
                        UPDATE_REQUEST_CODE
                    )
                }
            }
        })
    }

    /** Optional: route activity result back to the manager if needed in the future. */
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        // Currently no special handling; kept for future extension.
        return requestCode == UPDATE_REQUEST_CODE
    }
}
