package com.clawphones.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SecurityModuleTest {

    class TestLogger : Logger {
        val logs = mutableListOf<String>()
        override fun log(context: String, message: String) {
            logs.add("[$context] $message")
        }
    }

    @Test
    fun testEncryptDecryptRoundTripAndLogging() {
        val keySeed = "master-key-seed-1234"
        val testLogger = TestLogger()
        // Provide a fixed task_id via system property for test determinism
        System.setProperty("task_id", "G13-03-CP")
        val module = SecurityModule({ keySeed }, testLogger, { System.getProperty("task_id") ?: "unknown" })

        module.storeSensitive("password", "s3cr3tP@ss")
        val decrypted = module.retrieveSensitive("password")

        assertEquals("s3cr3tP@ss", decrypted)
        // verify that logs include task_id and the two operations
        val combined = testLogger.logs.joinToString("\n")
        assertTrue(combined.contains("task_id=G13-03-CP"))
        assertTrue(combined.contains("Stored encrypted value for key 'password'"))
        assertTrue(combined.contains("Retrieved decrypted value for key 'password'"))
    }
}
