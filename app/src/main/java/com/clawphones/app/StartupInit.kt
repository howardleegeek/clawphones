package com.clawphones.app

/**
 * Small helper to initialize non-critical subsystems on demand.
 * This helps push non-essential work off the critical path of cold-start.
 */
object StartupInit {
    @Volatile
    private var initialized = false

    fun initializeIfNeeded() {
        if (!initialized) {
            // Perform non-blocking, lightweight setup here.
            // Example: warm-up in-memory caches, prefetch configs, etc.
            initialized = true
            println("StartupInit: initialized lazily")
        }
    }
}
