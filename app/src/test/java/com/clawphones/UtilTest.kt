package com.clawphones

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UtilTest {

    @Test
    fun testGcdPositive() {
        assertEquals(6, Util.gcd(54, 24))
        assertEquals(1, Util.gcd(17, 13))
        // gcd(0,0) defined by implementation as 0
        assertEquals(0, Util.gcd(0, 0))
    }

    @Test
    fun testLcmPositive() {
        assertEquals(72, Util.lcm(12, 18))
        assertEquals(0, Util.lcm(0, 5))
        assertEquals(0, Util.lcm(0, 0))
    }

    @Test
    fun testToBinary() {
        assertEquals("1010", Util.toBinary(10))
        assertEquals("0", Util.toBinary(0))
        assertEquals("11111111", Util.toBinary(255))
    }
}
