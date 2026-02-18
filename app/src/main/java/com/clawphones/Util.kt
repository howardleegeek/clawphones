package com.clawphones

import kotlin.math.abs

/** Utility helpers used by tests. */
object Util {
    // Greatest common divisor using the Euclidean algorithm
    fun gcd(a: Int, b: Int): Int {
        var x = abs(a)
        var y = abs(b)
        while (y != 0) {
            val t = x % y
            x = y
            y = t
        }
        return x
    }

    // Least common multiple using gcd, gracefully handles zeros
    fun lcm(a: Int, b: Int): Int {
        if (a == 0 || b == 0) return 0
        return abs(a / gcd(a, b) * b)
    }

    // Simple conversion to binary string for testing purposes
    fun toBinary(n: Int): String {
        return Integer.toBinaryString(n)
    }
}
