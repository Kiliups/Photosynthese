package com.othregensburg.photosynthese.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.othregensburg.photosynthese.MainActivity
import com.othregensburg.photosynthese.R
import com.othregensburg.photosynthese.databinding.FragmentOnboardingLoginBinding
import com.othregensburg.photosynthese.models.userViewModel

class OnboardingLoginFragment : Fragment() {
    lateinit var binding: FragmentOnboardingLoginBinding
    val auth = FirebaseAuth.getInstance()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentOnboardingLoginBinding.inflate(inflater, container, false)
        binding.signUp.setOnClickListener {

        }
        val userVM= ViewModelProvider(requireActivity()).get(userViewModel::class.java)

        binding.loginButton.setOnClickListener {
            if (binding.user.text.toString().isNotEmpty() && binding.password.text.toString().isNotEmpty()) {
                userVM.login(binding.user.text.toString(), binding.password.text.toString())
                if (auth.currentUser != null) {
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
                }
            }
        }
        binding.signOutButton.setOnClickListener {
            userVM.signOut()
        }
        return binding.root
    }
}