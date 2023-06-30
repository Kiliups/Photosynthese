package com.othregensburg.photosynthese.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.othregensburg.photosynthese.databinding.FragmentOnboardingWelcomeBinding


class OnboardingWelcomeFragment : Fragment() {
    private lateinit var binding: FragmentOnboardingWelcomeBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentOnboardingWelcomeBinding.inflate(inflater, container, false)
        binding.getStartedButton.setOnClickListener {
            // Navigate to next fragment
            val onboardingActivity = requireActivity() as OnboardingActivity
            onboardingActivity.binding.viewPager.currentItem = 1
        }
        return binding.root
    }

}