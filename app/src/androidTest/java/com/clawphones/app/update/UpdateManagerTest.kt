package com.clawphones.app.update

import android.app.Activity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.mockito.Mockito.mock

class UpdateManagerTest {

    @Test
    fun promptsWhenUpdateAvailable() {
        val activity = mock(Activity::class.java)

        // Fake SDK that returns an available update
        val sdk = object : UpdateSDK {
            override fun checkForUpdate(callback: (UpdateInfo) -> Unit) {
                callback(UpdateInfo(true, "1.2.3"))
            }
            override fun startUpdateFlow(activity: Activity, updateType: Int, requestCode: Int) {
                // no-op
            }
        }

        val manager = UpdateManager(activity, { sdk })

        var seenInfo: UpdateInfo? = null
        manager.onUpdatePrompt = { info -> seenInfo = info }

        manager.checkForUpdate()

        assertNotNull(seenInfo)
        assertEquals(true, seenInfo!!.isAvailable)
        assertEquals("1.2.3", seenInfo!!.versionName)
    }

    @Test
    fun startUpdateFlowDelegatesToSDK() {
        val activity = mock(Activity::class.java)
        var recorded: Pair<Activity, Int>? = null
        val sdk = object : UpdateSDK {
            override fun checkForUpdate(callback: (UpdateInfo) -> Unit) {
                // do nothing
            }
            override fun startUpdateFlow(activityParam: Activity, updateType: Int, requestCode: Int) {
                recorded = Pair(activityParam, updateType)
            }
        }

        val manager = UpdateManager(activity, { sdk })
        manager.startUpdateFlow(AppUpdateType.IMMEDIATE)

        assertNotNull(recorded)
        assertEquals(activity, recorded!!.first)
        assertEquals(AppUpdateType.IMMEDIATE, recorded!!.second)
    }
}
