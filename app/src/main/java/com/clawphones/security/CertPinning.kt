package com.clawphones.security

import okhttp3.CertificatePinner

object CertPinning {
    private const val DEFAULT_PIN_SHA256 = "sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB="
    private const val BACKUP_PIN_SHA256 = "sha256/CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC="

    private val defaultPinners = mapOf(
        "api.clawphones.com" to listOf(DEFAULT_PIN_SHA256, BACKUP_PIN_SHA256),
        "auth.clawphones.com" to listOf(DEFAULT_PIN_SHA256, BACKUP_PIN_SHA256)
    )

    fun buildCertificatePinner(hostPins: Map<String, List<String>> = defaultPinners): CertificatePinner {
        val builder = CertificatePinner.Builder()
        hostPins.forEach { (host, pins) ->
            pins.forEach { pin ->
                builder.add(host, pin)
            }
        }
        return builder.build()
    }

    fun validateCertificate(host: String, certificateChain: List<String>): Boolean {
        val pins = defaultPinners[host] ?: return true
        return certificateChain.any { cert ->
            pins.any { pin -> cert.contains(pin.substringAfter("sha256/")) }
        }
    }

    fun getPinnedHosts(): Set<String> = defaultPinners.keys

    fun getPinsForHost(host: String): List<String> = defaultPinners[host] ?: emptyList()
}
