package com.clawphones.ui.accessibility

import org.junit.Assert.assertEquals
import org.junit.Test

class AccessibilityHelperTest {
    @Test
    fun sanitizeDescription_trimsWhitespace() {
        val input = "  1 accessible description  "
        val result = AccessibilityHelper.sanitizeDescription(input)
        assertEquals("1 accessible description", result)
    }

    @Test
    fun sanitizeDescription_nullReturnsEmpty() {
        val result = AccessibilityHelper.sanitizeDescription(null)
        assertEquals("", result)
    }

    @Test
    fun ensureDescription_withValidInput() {
        val input = "  Click here  "
        val result = AccessibilityHelper.ensureDescription(input)
        assertEquals("Click here", result)
    }

    @Test
    fun ensureDescription_withBlankInput() {
        val result = AccessibilityHelper.ensureDescription("   \t\n  ")
        assertEquals("No description provided", result)
    }
}
