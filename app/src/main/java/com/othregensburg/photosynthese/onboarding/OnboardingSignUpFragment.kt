package com.othregensburg.photosynthese.onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.othregensburg.photosynthese.R
import com.othregensburg.photosynthese.databinding.FragmentOnboardingSignUpBinding
import com.othregensburg.photosynthese.models.userViewModel

class OnboardingSignUpFragment : Fragment() {
    lateinit var binding: FragmentOnboardingSignUpBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentOnboardingSignUpBinding.inflate(inflater, container, false)
        val userVM = ViewModelProvider(requireActivity()).get(userViewModel::class.java)
        binding.signUpButton.setOnClickListener {
            if (binding.password.text.toString() == binding.confirmPassword.text.toString()
                && binding.username.text.toString().isNotEmpty()
                && binding.email.text.toString().isNotEmpty()
                && binding.password.text.toString().isNotEmpty()
                && binding.confirmPassword.text.toString().isNotEmpty()
                && binding.firstName.text.toString().isNotEmpty()
                && binding.lastName.text.toString().isNotEmpty()
                && binding.password.text.toString().length >= 6
            ) {
                //show error
                userVM.createUser(
                    binding.username.text.toString(),
                    binding.firstName.text.toString(),
                    binding.lastName.text.toString(),
                    binding.email.text.toString(),
                    binding.password.text.toString()
                )
            } else {
                //show error
                Toast.makeText(
                    requireContext(),
                    "Please fill out all fields and password must be at least 6 Characters",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        return binding.root
    }
}