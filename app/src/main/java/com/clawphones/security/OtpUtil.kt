package com.clawphones.security

import java.nio.ByteBuffer
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object OtpUtil {
    private val BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"

    private fun decodeBase32(base32: String): ByteArray {
        val cleaned = base32.replace("=", "").replace(" ", "").uppercase()
        val bytes = ArrayList<Byte>()
        var buffer = 0
        var bitsLeft = 0
        for (c in cleaned) {
            val valC = BASE32_CHARS.indexOf(c)
            if (valC < 0) continue
            buffer = (buffer shl 5) or valC
            bitsLeft += 5
            if (bitsLeft >= 8) {
                val b = (buffer shr (bitsLeft - 8)) and 0xFF
                bytes.add(b.toByte())
                bitsLeft -= 8
            }
        }
        val result = ByteArray(bytes.size)
        for (i in bytes.indices) result[i] = bytes[i]
        return result
    }

    fun generateTotp(secretBase32: String, timestampMillis: Long = System.currentTimeMillis(), digits: Int = 6, interval: Long = 30L): String {
        val key = decodeBase32(secretBase32)
        val counter = timestampMillis / (interval * 1000)
        val data = ByteBuffer.allocate(8).putLong(counter).array()
        val mac = Mac.getInstance("HmacSHA1")
        mac.init(SecretKeySpec(key, "HmacSHA1"))
        val hash = mac.doFinal(data)
        val offset = hash[hash.size - 1].toInt() and 0x0F
        val binary = ((hash[offset].toInt() and 0x7f) shl 24) or
                     ((hash[offset + 1].toInt() and 0xff) shl 16) or
                     ((hash[offset + 2].toInt() and 0xff) shl 8) or
                     (hash[offset + 3].toInt() and 0xff)
        val otp = binary % Math.pow(10.0, digits.toDouble()).toInt()
        return otp.toString().padStart(digits, '0')
    }

    fun verifyTotpIsPossible(otp: String, secretBase32: String, timestampMillis: Long = System.currentTimeMillis(), digits: Int = 6, interval: Long = 30L, tolerance: Int = 1): Boolean {
        val key = decodeBase32(secretBase32)
        val currentCounter = timestampMillis / (interval * 1000)
        val mac = Mac.getInstance("HmacSHA1")
        mac.init(SecretKeySpec(key, "HmacSHA1"))
        for (i in -tolerance..tolerance) {
            val counter = currentCounter + i
            val data = ByteBuffer.allocate(8).putLong(counter).array()
            val hash = mac.doFinal(data)
            val offset = hash[hash.size - 1].toInt() and 0x0F
            val binary = ((hash[offset].toInt() and 0x7f) shl 24) or
                         ((hash[offset + 1].toInt() and 0xff) shl 16) or
                         ((hash[offset + 2].toInt() and 0xff) shl 8) or
                         (hash[offset + 3].toInt() and 0xff)
            val otpVal = binary % Math.pow(10.0, digits.toDouble()).toInt()
            if (otpVal.toString().padStart(digits, '0') == otp) return true
        }
        return false
    }

    fun isOtpValid(otp: String, secretBase32: String, digits: Int = 6, tolerance: Int = 1): Boolean {
        return verifyTotpIsPossible(otp, secretBase32, System.currentTimeMillis(), digits, 30L, tolerance)
    }
}
