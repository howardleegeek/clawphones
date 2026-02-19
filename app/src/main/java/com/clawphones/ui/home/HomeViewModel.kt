package com.clawphones.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.clawphones.data.model.User

class HomeViewModel : ViewModel() {
    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user

    init {
        loadUser()
    }

    private fun loadUser() {
        _user.value = User(
            id = "1",
            name = "John Doe",
            email = "john.doe@example.com",
            phone = "+1-555-1234"
        )
    }

    fun updateUser(user: User) {
        _user.value = user
    }
}
