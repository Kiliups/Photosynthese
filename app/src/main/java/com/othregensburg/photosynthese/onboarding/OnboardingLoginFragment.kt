package com.othregensburg.photosynthese.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.othregensburg.photosynthese.MainActivity
import com.othregensburg.photosynthese.R
import com.othregensburg.photosynthese.databinding.FragmentOnboardingLoginBinding
import com.othregensburg.photosynthese.models.userViewModel

class OnboardingLoginFragment : Fragment() {
    private lateinit var binding: FragmentOnboardingLoginBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOnboardingLoginBinding.inflate(inflater, container, false)
        val userVM = ViewModelProvider(requireActivity()).get(userViewModel::class.java)
        binding.loginButton.setOnClickListener {
            // Check if all fields are filled
            if (binding.user.text.toString().isNotEmpty() && binding.password.text.toString()
                    .isNotEmpty()
            ) {
                // Login user
                userVM.login(binding.user.text.toString(), binding.password.text.toString())
                    .observe(viewLifecycleOwner) {
                        if (it == true) {
                            // if Login successful -> go to main activity
                            Toast.makeText(
                                requireContext(),
                                resources.getString(R.string.login_successful),
                                Toast.LENGTH_SHORT
                            ).show()
                            val intent = Intent(requireContext(), MainActivity::class.java)
                            startActivity(intent)
                            requireActivity().finish()
                        } else {
                            // if Login failed -> show error
                            Toast.makeText(
                                requireContext(),
                                resources.getString(R.string.login_failed),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }
        binding.signUp.setOnClickListener {
            // Go to sign up fragment
            val onboardingActivity = requireActivity() as OnboardingActivity
            onboardingActivity.binding.viewPager.currentItem = 2
        }
        binding.backButton.setOnClickListener {
            // Go to previous fragment
            val onboardingActivity = requireActivity() as OnboardingActivity
            onboardingActivity.binding.viewPager.currentItem = 0
        }
        return binding.root
    }
}
