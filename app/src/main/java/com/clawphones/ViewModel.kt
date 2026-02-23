package com.clawphones

// Lightweight ViewModel substitute for testing purposes in this kata.
// This is a minimal implementation to allow unit tests to run without
// depending on Android Architecture Components.
class ViewModel(initialState: Int = 0) {
    private var state: Int = initialState

    fun getState(): Int = state

    fun increment() {
        state += 1
    }

    fun reset() {
        state = 0
    }
}
