package com.othregensburg.photosynthese.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.othregensburg.photosynthese.MainActivity
import com.othregensburg.photosynthese.R
import com.othregensburg.photosynthese.databinding.FragmentOnboardingLoginBinding

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
        binding.loginButton.setOnClickListener {
            auth.signInWithEmailAndPassword(
                binding.user.text.toString(),
                binding.password.text.toString()
            ).addOnCompleteListener {
                if (it.isSuccessful) {
                    //start main activity
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
                } else {
                    //show error
                }
            }
        }
        return binding.root
    }
}