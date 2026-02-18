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
    CHECKBOX
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
}
