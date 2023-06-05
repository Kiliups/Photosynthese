package com.othregensburg.photosynthese.onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.othregensburg.photosynthese.R
import com.othregensburg.photosynthese.databinding.FragmentOnboardingSignUpBinding

class OnboardingSignUpFragment : Fragment() {
    lateinit var binding: FragmentOnboardingSignUpBinding
    val auth = FirebaseAuth.getInstance()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= FragmentOnboardingSignUpBinding.inflate(inflater, container, false)
        binding.signUpButton.setOnClickListener {
            auth.createUserWithEmailAndPassword(
                binding.user.text.toString(),
                binding.password.text.toString()
            ).addOnCompleteListener {
                if (it.isSuccessful) {
                    //start main activity
                } else {
                    //show error
                }
            }
        }
        return binding.root
    }
}