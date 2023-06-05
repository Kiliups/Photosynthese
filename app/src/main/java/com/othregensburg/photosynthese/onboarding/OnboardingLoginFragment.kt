package com.othregensburg.photosynthese.onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.othregensburg.photosynthese.R
import com.othregensburg.photosynthese.databinding.FragmentOnboardingLoginBinding

class OnboardingLoginFragment : Fragment() {
    lateinit var binding: FragmentOnboardingLoginBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= FragmentOnboardingLoginBinding.inflate(inflater, container, false)
        binding.signUp.setOnClickListener{
            val fragment = OnboardingSignUpFragment()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame_layout, fragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
        return binding.root
    }
}