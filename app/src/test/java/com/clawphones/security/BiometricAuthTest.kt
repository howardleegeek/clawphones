package com.clawphones.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BiometricAuthTest {

    @Test
    fun biometricStatus_enum_hasAllExpectedValues() {
        assertEquals(5, BiometricAuth.BiometricStatus.entries.size)
        assertNotNull(BiometricAuth.BiometricStatus.AVAILABLE)
        assertNotNull(BiometricAuth.BiometricStatus.NO_HARDWARE)
        assertNotNull(BiometricAuth.BiometricStatus.HARDWARE_UNAVAILABLE)
        assertNotNull(BiometricAuth.BiometricStatus.NOT_ENROLLED)
        assertNotNull(BiometricAuth.BiometricStatus.UNKNOWN)
    }

    @Test
    fun biometricError_dataClass_holdsCorrectValues() {
        val error = BiometricAuth.BiometricError(10, "Test error message")
        assertEquals(10, error.code)
        assertEquals("Test error message", error.message)
    }

    @Test
    fun biometricError_dataClass_supportsEquality() {
        val error1 = BiometricAuth.BiometricError(10, "Test error")
        val error2 = BiometricAuth.BiometricError(10, "Test error")
        val error3 = BiometricAuth.BiometricError(20, "Different")
        
        assertEquals(error1, error2)
        assertEquals(error1.hashCode(), error2.hashCode())
        assert(error1 != error3)
    }

    @Test
    fun biometricAuthListener_interfaceHasRequiredMethods() {
        val listener = object : BiometricAuth.BiometricAuthListener {
            override fun onSuccess(result: androidx.biometric.BiometricPrompt.AuthenticationResult) {}
            override fun onError(errorCode: Int, errorMessage: String) {}
            override fun onFailed() {}
        }
        assertNotNull(listener)
    }

    @Test
    fun biometricAuth_hasDeviceCredentialFallbackMethod() {
        assertTrue(BiometricAuth::class.java.methods.any { 
            it.name == "authenticateWithDeviceCredentialFallback" && 
            it.parameterTypes.size == 2 &&
            it.parameterTypes[0] == String::class.java &&
            it.parameterTypes[1] == String::class.java
        })
    }
}
