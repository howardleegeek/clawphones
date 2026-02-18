package com.clawphones.ui.main

import org.junit.Assert.assertEquals
import org.junit.Test

class MainActivityTest {
    @Test
    fun testLoginDisplayTextLoggedOut() {
        assertEquals("登录", loginDisplayText(false))
    }

    @Test
    fun testLoginDisplayTextLoggedIn() {
        assertEquals("已登录", loginDisplayText(true))
    }
}
