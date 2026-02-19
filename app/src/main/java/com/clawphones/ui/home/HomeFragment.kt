package com.clawphones.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.clawphones.data.model.User

class HomeFragment : Fragment() {
    private val viewModel: HomeViewModel by viewModels()
    
    private lateinit var userNameTextView: TextView
    private lateinit var userEmailTextView: TextView
    private lateinit var userPhoneTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        userNameTextView = view.findViewById(R.id.text_user_name)
        userEmailTextView = view.findViewById(R.id.text_user_email)
        userPhoneTextView = view.findViewById(R.id.text_user_phone)
        
        observeUserData()
    }

    private fun observeUserData() {
        viewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let { updateUI(it) }
        }
    }

    private fun updateUI(user: User) {
        userNameTextView.text = user.name
        userEmailTextView.text = user.email
        userPhoneTextView.text = user.phone
    }
}
