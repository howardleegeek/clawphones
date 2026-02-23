package com.clawphones.ui.main

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class MainActivityTest {
    @Test
    fun tokenValidGoesToMain() = runBlocking {
        val manager = LoginStateManager({ "VALID_TOKEN" }, DefaultTokenValidator())
        val dest = manager.startupDestination()
        assertEquals(StartupDestination.MAIN, dest)
    }

    @Test
    fun nullTokenGoesToLogin() = runBlocking {
        val manager = LoginStateManager({ null }, DefaultTokenValidator())
        val dest = manager.startupDestination()
        assertEquals(StartupDestination.LOGIN, dest)
    }

    @Test
    fun expiredTokenGoesToLogin() = runBlocking {
        val manager = LoginStateManager({ "expired" }, DefaultTokenValidator())
        val dest = manager.startupDestination()
        assertEquals(StartupDestination.LOGIN, dest)
    }

    @Test
    fun blankTokenGoesToLogin() = runBlocking {
        val manager = LoginStateManager({ "" }, DefaultTokenValidator())
        val dest = manager.startupDestination()
        assertEquals(StartupDestination.LOGIN, dest)
    }
}
