package com.clawphones

import org.junit.Assert.assertEquals
import org.junit.Test

class ViewModelTest {
    @Test
    fun initialStateIsSet() {
        val vm = ViewModel(5)
        assertEquals(5, vm.getState())
    }

    @Test
    fun incrementIncreasesState() {
        val vm = ViewModel(2)
        vm.increment()
        assertEquals(3, vm.getState())
    }

    @Test
    fun resetSetsStateToZero() {
        val vm = ViewModel(7)
        vm.reset()
        assertEquals(0, vm.getState())
    }
}
