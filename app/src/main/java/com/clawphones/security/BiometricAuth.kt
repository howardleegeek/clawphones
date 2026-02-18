package com.clawphones.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executor

class BiometricAuth(private val activity: FragmentActivity) {

    private val executor: Executor = ContextCompat.getMainExecutor(activity)
    private val biometricPrompt: BiometricPrompt by lazy {
        BiometricPrompt(activity, executor, callback)
    }
    private val promptInfo: BiometricPrompt.PromptInfo by lazy {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Authentication")
            .setSubtitle("Verify your identity")
            .setNegativeButtonText("Cancel")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()
    }

    private val callback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            lastError = BiometricError(errorCode, errString.toString())
            listener?.onError(errorCode, errString.toString())
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            lastError = null
            listener?.onSuccess(result)
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            listener?.onFailed()
        }
    }

    private var listener: BiometricAuthListener? = null
    private var lastError: BiometricError? = null

    fun setListener(listener: BiometricAuthListener) {
        this.listener = listener
    }

    fun canAuthenticate(): BiometricStatus {
        val biometricManager = BiometricManager.from(activity)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricStatus.AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricStatus.NO_HARDWARE
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricStatus.HARDWARE_UNAVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricStatus.NOT_ENROLLED
            else -> BiometricStatus.UNKNOWN
        }
    }

    fun authenticate() {
        biometricPrompt.authenticate(promptInfo)
    }

    fun authenticateWithTitle(title: String, subtitle: String, negativeButtonText: String) {
        val customPromptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()
        biometricPrompt.authenticate(customPromptInfo)
    }

    fun authenticateWithDeviceCredentialFallback(title: String, subtitle: String) {
        val customPromptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()
        biometricPrompt.authenticate(customPromptInfo)
    }

    fun getLastError(): BiometricError? = lastError

    fun cancelAuthentication() {
        biometricPrompt.cancelAuthentication()
    }

    interface BiometricAuthListener {
        fun onSuccess(result: BiometricPrompt.AuthenticationResult)
        fun onError(errorCode: Int, errorMessage: String)
        fun onFailed()
    }

    data class BiometricError(val code: Int, val message: String)

    enum class BiometricStatus {
        AVAILABLE,
        NO_HARDWARE,
        HARDWARE_UNAVAILABLE,
        NOT_ENROLLED,
        UNKNOWN
    }

    companion object {
        @Volatile
        private var instance: BiometricAuth? = null

        fun getInstance(activity: FragmentActivity): BiometricAuth {
            return instance ?: synchronized(this) {
                instance ?: BiometricAuth(activity).also { instance = it }
            }
        }
    }
}
