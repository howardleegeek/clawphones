package com.clawphones.app.update

import android.app.Activity
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.tasks.OnSuccessListener
import com.google.android.play.core.tasks.Task
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*

/** Instrumentation-like test for UpdateManager using mocks. */
class UpdateManagerTest {

    @Test
    fun triggersUpdateFlowWhenUpdateAvailable() {
        val mockActivity = mock(Activity::class.java)
        val mockAppUpdateManager = mock(AppUpdateManager::class.java)
        val mockTask = mock(Task::class.java) as Task<AppUpdateInfo>
        val mockInfo = mock(AppUpdateInfo::class.java)

        // Setup task to return mockInfo via onSuccess callback
        `when`(mockAppUpdateManager.appUpdateInfo).thenReturn(mockTask)
        `when`(mockInfo.updateAvailability()).thenReturn(UpdateAvailability.UPDATE_AVAILABLE)
        `when`(mockInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)).thenReturn(true)

        doAnswer { invocation ->
            val listener = invocation.getArgument<OnSuccessListener<AppUpdateInfo>>(0)
            listener.onSuccess(mockInfo)
            null
        }.`when`(mockTask).addOnSuccessListener(any())

        val updater = UpdateManager(mockActivity, mockAppUpdateManager)
        updater.checkForUpdate()

        verify(mockAppUpdateManager).startUpdateFlow(
            eq(mockInfo), eq(AppUpdateType.IMMEDIATE), eq(mockActivity), anyInt()
        )
    }
}
