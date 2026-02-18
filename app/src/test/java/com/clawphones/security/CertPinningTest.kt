package com.clawphones.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CertPinningTest {

    @Test
    fun testGetPinnedHosts() {
        val pinnedHosts = CertPinning.getPinnedHosts()
        assertTrue(pinnedHosts.contains("api.clawphones.com"))
        assertTrue(pinnedHosts.contains("auth.clawphones.com"))
        assertEquals(2, pinnedHosts.size)
    }

    @Test
    fun testGetPinsForKnownHost() {
        val pins = CertPinning.getPinsForHost("api.clawphones.com")
        assertEquals(2, pins.size)
        assertTrue(pins[0].startsWith("sha256/"))
    }

    @Test
    fun testGetPinsForUnknownHost() {
        val pins = CertPinning.getPinsForHost("unknown.example.com")
        assertTrue(pins.isEmpty())
    }

    @Test
    fun testValidateCertificateWithMatchingPin() {
        val certChain = listOf("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB=")
        val result = CertPinning.validateCertificate("api.clawphones.com", certChain)
        assertTrue(result)
    }

    @Test
    fun testValidateCertificateWithNoMatchingPin() {
        val certChain = listOf("DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD=")
        val result = CertPinning.validateCertificate("api.clawphones.com", certChain)
        assertFalse(result)
    }

    @Test
    fun testValidateCertificateForUnknownHost() {
        val certChain = listOf("any-cert")
        val result = CertPinning.validateCertificate("unknown.example.com", certChain)
        assertTrue(result)
    }

    @Test
    fun testBuildCertificatePinner() {
        val customPins = mapOf(
            "custom.example.com" to listOf("sha256/customPin1=", "sha256/customPin2=")
        )
        val pinner = CertPinning.buildCertificatePinner(customPins)
        assertTrue(pinner.toString().contains("custom.example.com"))
    }
}
