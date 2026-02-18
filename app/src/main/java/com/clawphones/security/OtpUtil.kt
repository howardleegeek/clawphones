package com.clawphones.security

/** Utility for generating and validating TOTP codes. */
object OtpUtil {
    private const val BASE32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"

    // Generate a 16-byte secret and encode as base32 for easy sharing.
    fun generateSecret(): String {
        val random = java.security.SecureRandom()
        val bytes = ByteArray(16)
        random.nextBytes(bytes)
        return base32Encode(bytes)
    }

    // Generate a 6-digit OTP for the given secret and timestamp (ms since epoch).
    fun generateOtp(secret: String, timestamp: Long = System.currentTimeMillis()): String {
        val secretBytes = base32Decode(secret)
        val timeStep = (timestamp / 1000L) / 30L
        val data = java.nio.ByteBuffer.allocate(8).putLong(timeStep).array()

        val signingKey = javax.crypto.spec.SecretKeySpec(secretBytes, "HmacSHA1")
        val mac = javax.crypto.Mac.getInstance("HmacSHA1")
        mac.init(signingKey)
        val hash = mac.doFinal(data)

        val offset = hash[hash.size - 1].toInt() and 0x0F
        val binary = ((hash[offset].toInt() and 0x7f) shl 24) or
                ((hash[offset + 1].toInt() and 0xff) shl 16) or
                ((hash[offset + 2].toInt() and 0xff) shl 8) or
                (hash[offset + 3].toInt() and 0xff)
        val otp = binary % 1_000_000
        return String.format("%06d", otp)
    }

    // Base32 decode
    private fun base32Decode(input: String): ByteArray {
        val clean = input.trim().replace("=", "").replace(" ", "").uppercase()
        val result = ArrayList<Byte>()
        var buffer = 0
        var bitsLeft = 0
        for (c in clean) {
            val idx = BASE32_ALPHABET.indexOf(c)
            if (idx == -1) continue
            buffer = (buffer shl 5) or idx
            bitsLeft += 5
            while (bitsLeft >= 8) {
                val b = (buffer shr (bitsLeft - 8)) and 0xFF
                result.add(b.toByte())
                bitsLeft -= 8
            }
        }
        return result.toByteArray()
    }

    // Base32 encode
    private fun base32Encode(bytes: ByteArray): String {
        val sb = StringBuilder()
        var buffer = 0
        var bitsLeft = 0
        for (b in bytes) {
            buffer = (buffer shl 8) or (b.toInt() and 0xFF)
            bitsLeft += 8
            while (bitsLeft >= 5) {
                val idx = (buffer shr (bitsLeft - 5)) and 0x1F
                sb.append(BASE32_ALPHABET[idx])
                bitsLeft -= 5
            }
        }
        if (bitsLeft > 0) {
            val idx = (buffer shl (5 - bitsLeft)) and 0x1F
            sb.append(BASE32_ALPHABET[idx])
        }
        return sb.toString()
    }
}
