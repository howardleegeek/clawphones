package com.clawphones.ui.main

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MainActivityTest {

    @Test
    fun testLogout_changesLoggedInStateToFalse() {
        val activity = MainActivity()
        
        activity.performLogout()
        
        assertFalse(activity.isUserLoggedIn())
    }

    @Test
    fun testLogin_changesLoggedInStateToTrue() {
        val activity = MainActivity()
        activity.performLogout()
        
        activity.performLogin()
        
        assertTrue(activity.isUserLoggedIn())
    }

    @Test
    fun testInitialState_isLoggedIn() {
        val activity = MainActivity()
        
        assertTrue(activity.isUserLoggedIn())
    }

    @Test
    fun testLogoutThenLogin_togglesStateCorrectly() {
        val activity = MainActivity()
        
        activity.performLogout()
        assertFalse(activity.isUserLoggedIn())
        
        activity.performLogin()
        assertTrue(activity.isUserLoggedIn())
    }
}
