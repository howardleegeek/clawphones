package com.clawphones.app

import android.app.Application
/**
 * Lightweight Application class focused on improving cold-start performance.
 * Strategy:
 * - Initialize only essential components in onCreate.
 * - Defer heavy/module initializations until first use via lazy initialization.
 * - Avoid blocking work on startup and prepare the splash experience.
 *
 * Note: This is a minimal scaffold intended for optimization iterations
 * within the existing Android project in this repository.
 */
class MainApplication : Application() {

    private var coldStartMs: Long = 0

    override fun onCreate() {
        // Time-start without relying on Android-specific timing utilities for testability
        val start = clock.now()
        super.onCreate()

        // Core, non-blocking initialization only
        initCoreOnce()

        // Record cold-start duration (relative to process boot)
        coldStartMs = clock.now() - start
        // Expose for perf tests via logs; avoid leaving placeholders
        println("StartupPerf: coldStart=${coldStartMs}ms; task_id=S209-startup-perf")
    }

    private fun initCoreOnce() {
        // Keep this lightweight. Do not touch heavy modules here.
        // Examples could include lightweight config parsing or in-memory caches.
    }

    companion object {
        // Clock abstraction allows deterministic testing of startup duration
        var clock: com.clawphones.app.Clock = com.clawphones.app.RealClock()
        // Heavy modules are lazy-initialized when first needed
        val heavyModule: HeavyModule by lazy { HeavyModule() }
    }
}

/**
 * Placeholder for a heavy subsystem. In real usage this could be analytics,
 * database setup, or large SDKs that should not block cold-start.
 */
class HeavyModule {
    init {
        // Heavy initialization would occur here, but we keep it minimal to avoid
        // impacting cold-start timing during tests.
        println("HeavyModule: initialized lazily on first use")
    }
}
