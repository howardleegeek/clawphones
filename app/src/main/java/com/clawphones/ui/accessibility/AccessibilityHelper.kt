package com.clawphones.ui.accessibility

/** Accessibility helper utilities. These are pure Kotlin utilities to
 * aid in producing accessible descriptions for UI elements without
 * tying directly to Android framework in unit tests.
 */
enum class AccessibleRole {
    BUTTON,
    SWITCH,
    SLIDER,
    IMAGE,
    TEXT,
    CHECKBOX,
    LINK,
    RADIO_BUTTON,
    TOGGLE,
    PROGRESS_INDICATOR,
    TAB,
    MENU_ITEM,
    EDIT_TEXT
}

object AccessibilityHelper {
    /** Build an accessible description by combining a label with a role suffix. */
    fun toAccessibleDescription(label: String, role: AccessibleRole): String {
        val suffix = when (role) {
            AccessibleRole.BUTTON -> "button"
            AccessibleRole.SWITCH -> "switch"
            AccessibleRole.SLIDER -> "slider"
            AccessibleRole.IMAGE -> "image"
            AccessibleRole.TEXT -> "text"
            AccessibleRole.CHECKBOX -> "checkbox"
            AccessibleRole.LINK -> "link"
            AccessibleRole.RADIO_BUTTON -> "radio button"
            AccessibleRole.TOGGLE -> "toggle"
            AccessibleRole.PROGRESS_INDICATOR -> "progress indicator"
            AccessibleRole.TAB -> "tab"
            AccessibleRole.MENU_ITEM -> "menu item"
            AccessibleRole.EDIT_TEXT -> "text field"
        }
        val trimmed = label.trim()
        return if (trimmed.isEmpty()) suffix else "$trimmed $suffix"
    }

    /** Simple normalization for accessibility labels (e.g., capitalization). */
    fun normalizeForAccessibility(text: String): String {
        return text.trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }

    /** Normalize or sanitize a description string for accessibility tests. */
    // - If input is null, return empty string (tests expect this behavior).
    fun sanitizeDescription(input: String?): String {
        return input?.trim() ?: ""
    }

    /** Ensure a non-empty description, providing a default when blank. */
    fun ensureDescription(input: String?): String {
        val trimmed = sanitizeDescription(input)
        return if (trimmed.isEmpty()) "No description provided" else trimmed
    }

    /** Build a state description for interactive elements (e.g., on/off for switches). */
    fun buildStateDescription(isOn: Boolean): String {
        return if (isOn) "on" else "off"
    }

    /** Combine a label with its state for interactive elements. */
    fun buildStatefulDescription(label: String, role: AccessibleRole, isOn: Boolean): String {
        val baseDescription = toAccessibleDescription(label, role)
        val stateSuffix = buildStateDescription(isOn)
        return "$baseDescription, $stateSuffix"
    }

    /** Validate that a content description meets minimum accessibility requirements. */
    fun validateContentDescription(description: String?): ValidationResult {
        val trimmed = sanitizeDescription(description)
        return when {
            trimmed.isEmpty() -> ValidationResult.EMPTY
            trimmed.length < MIN_CONTENT_LENGTH -> ValidationResult.TOO_SHORT
            else -> ValidationResult.VALID
        }
    }

    /** Build a compound description from multiple parts, filtering empty ones. */
    fun buildCompoundDescription(vararg parts: String?): String {
        return parts.mapNotNull { sanitizeDescription(it).takeIf { s -> s.isNotEmpty() } }
            .joinToString(", ")
    }

    /** Check if text meets minimum accessibility length requirements. */
    fun isValidAccessibilityText(text: String?): Boolean {
        return validateContentDescription(text) == ValidationResult.VALID
    }

    enum class ValidationResult {
        VALID,
        EMPTY,
        TOO_SHORT
    }

    companion object {
        private const val MIN_CONTENT_LENGTH = 2
    }
}
