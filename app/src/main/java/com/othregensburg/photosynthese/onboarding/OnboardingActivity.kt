package com.othregensburg.photosynthese.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.othregensburg.photosynthese.MainActivity
import com.othregensburg.photosynthese.databinding.ActivityOnboardingBinding

class OnboardingActivity : AppCompatActivity() {
    lateinit var binding: ActivityOnboardingBinding
    private val auth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val fragmentList = arrayListOf(
            OnboardingWelcomeFragment(),
            OnboardingLoginFragment(),
            OnboardingSignUpFragment()
        )
        val adapter = ViewPagerAdapter(supportFragmentManager, lifecycle, fragmentList)
        binding.viewPager.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        // Check if user is already logged in
        if (auth.currentUser != null) {
            // start main activity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
