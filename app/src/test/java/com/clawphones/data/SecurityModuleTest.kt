package com.clawphones.data

import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertContentEquals

class SecurityModuleTest {
    private fun join(parts: List<ByteArray>): ByteArray {
        val total = parts.sumOf { it.size }
        val out = ByteArray(total)
        var pos = 0
        for (p in parts) {
            System.arraycopy(p, 0, out, pos, p.size)
            pos += p.size
        }
        return out
    }

    @Test
    fun testEncryptDecryptFlowRoundTrip() = runBlocking {
        // Initialize with a deterministic key derived from a string
        SecurityModule.setKeyFromString("gen9-test-key")

        val originalChunks = listOf(
            "Hello ".toByteArray(),
            "world".toByteArray(),
            " this is a test".toByteArray()
        )

        val sourceFlow = originalChunks.asFlow()
        val encrypted = SecurityModule.encryptFlow(sourceFlow)
        val decrypted = SecurityModule.decryptFlow(encrypted)

        val decryptedChunks = decrypted.toList()
        val decryptedBytes = join(decryptedChunks)
        val expectedBytes = join(originalChunks)

        assertContentEquals(expectedBytes, decryptedBytes, "Decrypted data should match original data")
    }
}
