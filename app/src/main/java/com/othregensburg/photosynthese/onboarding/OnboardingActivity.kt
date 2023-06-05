package com.othregensburg.photosynthese.onboarding

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.othregensburg.photosynthese.R
import com.othregensburg.photosynthese.databinding.ActivityOnboardingBinding

class OnboardingActivity : AppCompatActivity() {
    lateinit var binding: ActivityOnboardingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val fragmentList = arrayListOf<Fragment>(
            OnboardingWelcomeFragment(),
            OnboardingLoginFragment()
        )
        val adapter = ViewPagerAdapter(supportFragmentManager, lifecycle, fragmentList)
        binding.viewPager.adapter = adapter
    }
}