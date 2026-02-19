package com.clawphones.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.clawphones.data.model.User
import com.clawphones.util.Logger

class HomeViewModel : ViewModel() {
    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user

    companion object {
        private const val TAG = "HomeViewModel-G14-CP"
    }
    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user

    init {
        loadUser()
    }

    private fun loadUser() {
        val initial = User(
            id = "1",
            name = "John Doe",
            email = "john.doe@example.com",
            phone = "+1-555-1234"
        )
        _user.value = initial
        Logger.d(TAG, "task_id=G14-06-CP loadUser: initial user set to ${initial}")
    }

    fun updateUser(user: User) {
        _user.value = user
        Logger.d(TAG, "task_id=G14-06-CP updateUser: updated to ${user.id}")
    }
}
