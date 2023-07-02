package com.othregensburg.photosynthese.onboarding

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.othregensburg.photosynthese.R
import com.othregensburg.photosynthese.databinding.FragmentOnboardingSignUpBinding
import com.othregensburg.photosynthese.models.userViewModel

class OnboardingSignUpFragment : Fragment() {
    private lateinit var binding: FragmentOnboardingSignUpBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentOnboardingSignUpBinding.inflate(inflater, container, false)
        val userVM = ViewModelProvider(requireActivity()).get(userViewModel::class.java)
        binding.signUpButton.setOnClickListener {
            // Check if all fields are filled
            if (binding.username.text.toString().isNotEmpty() &&
                binding.email.text.toString().isNotEmpty() &&
                binding.password.text.toString().isNotEmpty() &&
                binding.confirmPassword.text.toString().isNotEmpty() &&
                binding.firstName.text.toString().isNotEmpty() &&
                binding.lastName.text.toString().isNotEmpty()
            ) {
                // Check if password is at least 6 characters long
                if (binding.password.text.toString().length >= 6) {
                    // Check if passwords match
                    if (binding.password.text.toString() == binding.confirmPassword.text.toString()) {
                        // Check if email is valid
                        if (Patterns.EMAIL_ADDRESS.matcher(binding.email.text).matches()) {
                            // Create user
                            userVM.createUser(
                                binding.username.text.toString(),
                                binding.firstName.text.toString(),
                                binding.lastName.text.toString(),
                                binding.email.text.toString(),
                                binding.password.text.toString()
                            )
                            val onboardingActivity = requireActivity() as OnboardingActivity
                            onboardingActivity.binding.viewPager.currentItem = 1
                        } else {
                            // Show error if email is invalid
                            Toast.makeText(
                                requireContext(),
                                resources.getString(R.string.invalid_email),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        // Show error if passwords don't match
                        Toast.makeText(
                            requireContext(),
                            resources.getString(R.string.invalid_confirm_password),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    // Show error if password is too short
                    Toast.makeText(
                        requireContext(),
                        resources.getString(R.string.invalid_password),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                // Show error if not all fields are filled
                Toast.makeText(
                    requireContext(),
                    resources.getString(R.string.all_fields),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        binding.login.setOnClickListener {
            // Go to login page
            val onboardingActivity = requireActivity() as OnboardingActivity
            onboardingActivity.binding.viewPager.currentItem = 1
        }
        binding.backButton.setOnClickListener {
            // Go to previous page
            val onboardingActivity = requireActivity() as OnboardingActivity
            onboardingActivity.binding.viewPager.currentItem = 1
        }
        return binding.root
    }
}
