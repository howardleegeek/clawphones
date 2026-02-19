package com.clawphones.util

/** Simple cross-platform logger placeholder.
 *  Prints to stdout to satisfy test and basic logging requirements without
 *  depending on Android logging in unit tests.
 */
object Logger {
  fun d(tag: String, message: String) {
    println("DEBUG: [$tag] $message")
  }
}
