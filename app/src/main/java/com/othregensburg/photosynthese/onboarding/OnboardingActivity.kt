package com.othregensburg.photosynthese.onboarding

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.othregensburg.photosynthese.MainActivity
import com.othregensburg.photosynthese.R
import com.othregensburg.photosynthese.databinding.ActivityOnboardingBinding

class OnboardingActivity : AppCompatActivity() {
    lateinit var binding: ActivityOnboardingBinding
    val auth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val fragmentList = arrayListOf<Fragment>(
            OnboardingWelcomeFragment(),
            OnboardingLoginFragment(),
            OnboardingSignUpFragment()
        )
        val adapter = ViewPagerAdapter(supportFragmentManager, lifecycle, fragmentList)
        binding.viewPager.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        /*if (auth.currentUser != null) {
            //start main activity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }*/
    }
}