package com.clawphones.app

/**
 * Clock abstraction to enable deterministic startup timing tests.
 */
interface Clock {
    fun now(): Long
}

/** Real clock implementation using monotonic time, suitable for tests and production. */
class RealClock : Clock {
    override fun now(): Long = java.lang.System.nanoTime() / 1_000_000 // convert to ms
}

/**
 * Startup timing helper that measures the duration of a core initialization block.
 * This is test-friendly and does not perform any Android-specific work.
 */
class StartupPerfEngine(private val clock: Clock) {
    fun measureStartup(coreInit: () -> Unit): Long {
        val start = clock.now()
        coreInit()
        return clock.now() - start
    }
}
