package com.clawphones.ui.accessibility

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
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

    @Test
    fun toAccessibleDescription_withNewRoles() {
        assertEquals("click link", AccessibilityHelper.toAccessibleDescription("click", AccessibleRole.LINK))
        assertEquals("option radio button", AccessibilityHelper.toAccessibleDescription("option", AccessibleRole.RADIO_BUTTON))
        assertEquals("settings toggle", AccessibilityHelper.toAccessibleDescription("settings", AccessibleRole.TOGGLE))
        assertEquals("loading progress indicator", AccessibilityHelper.toAccessibleDescription("loading", AccessibleRole.PROGRESS_INDICATOR))
        assertEquals("home tab", AccessibilityHelper.toAccessibleDescription("home", AccessibleRole.TAB))
        assertEquals("save menu item", AccessibilityHelper.toAccessibleDescription("save", AccessibleRole.MENU_ITEM))
        assertEquals("username text field", AccessibilityHelper.toAccessibleDescription("username", AccessibleRole.EDIT_TEXT))
    }

    @Test
    fun buildStateDescription_onState() {
        assertEquals("on", AccessibilityHelper.buildStateDescription(true))
    }

    @Test
    fun buildStateDescription_offState() {
        assertEquals("off", AccessibilityHelper.buildStateDescription(false))
    }

    @Test
    fun buildStatefulDescription_switchOn() {
        val result = AccessibilityHelper.buildStatefulDescription("WiFi", AccessibleRole.SWITCH, true)
        assertEquals("WiFi switch, on", result)
    }

    @Test
    fun buildStatefulDescription_switchOff() {
        val result = AccessibilityHelper.buildStatefulDescription("WiFi", AccessibleRole.SWITCH, false)
        assertEquals("WiFi switch, off", result)
    }

    @Test
    fun validateContentDescription_valid() {
        val result = AccessibilityHelper.validateContentDescription("Valid description")
        assertEquals(AccessibilityHelper.ValidationResult.VALID, result)
    }

    @Test
    fun validateContentDescription_empty() {
        val result = AccessibilityHelper.validateContentDescription("")
        assertEquals(AccessibilityHelper.ValidationResult.EMPTY, result)
    }

    @Test
    fun validateContentDescription_null() {
        val result = AccessibilityHelper.validateContentDescription(null)
        assertEquals(AccessibilityHelper.ValidationResult.EMPTY, result)
    }

    @Test
    fun validateContentDescription_tooShort() {
        val result = AccessibilityHelper.validateContentDescription("A")
        assertEquals(AccessibilityHelper.ValidationResult.TOO_SHORT, result)
    }

    @Test
    fun buildCompoundDescription_multipleParts() {
        val result = AccessibilityHelper.buildCompoundDescription("Part1", "Part2", "Part3")
        assertEquals("Part1, Part2, Part3", result)
    }

    @Test
    fun buildCompoundDescription_withEmptyParts() {
        val result = AccessibilityHelper.buildCompoundDescription("Part1", null, "", "  ", "Part2")
        assertEquals("Part1, Part2", result)
    }

    @Test
    fun isValidAccessibilityText_valid() {
        assertTrue(AccessibilityHelper.isValidAccessibilityText("Valid text"))
    }

    @Test
    fun isValidAccessibilityText_invalid() {
        assertFalse(AccessibilityHelper.isValidAccessibilityText(""))
        assertFalse(AccessibilityHelper.isValidAccessibilityText(null))
        assertFalse(AccessibilityHelper.isValidAccessibilityText("A"))
    }
}
