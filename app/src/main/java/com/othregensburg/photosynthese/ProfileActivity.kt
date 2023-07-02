package com.othregensburg.photosynthese

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.othregensburg.photosynthese.databinding.ActivityProfileBinding
import com.othregensburg.photosynthese.models.userViewModel
import com.othregensburg.photosynthese.onboarding.OnboardingActivity

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private val auth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val uVM = ViewModelProvider(this).get(userViewModel::class.java)
        var selectedPicture: Uri? = null

        uVM.isDone.observe(this) {
            if (it == true) {
                Toast.makeText(
                    this,
                    resources.getString(R.string.profile_update),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this,
                    resources.getString(R.string.profile_update_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        // get user data
        uVM.getUser(auth.currentUser!!.uid).observe(this) { user ->
            if (user != null) {
                // set user data
                binding.username.setText(user.username)
                binding.firstName.setText(user.firstname)
                binding.lastName.setText(user.lastname)
                binding.email.setText(user.email)

                // set profile picture if available
                if (user.picture != null) {
                    Glide.with(this).load(user.picture).into(binding.profilePicture)
                }

                // set on click listener for check button and update user
                binding.checkButton.setOnClickListener {
                    uVM.updateUser(
                        auth.currentUser!!.uid,
                        binding.username.text.toString(),
                        user.email,
                        binding.firstName.text.toString(),
                        binding.lastName.text.toString(),
                        selectedPicture
                    )
                }
            }
        }

        // set on click listener for back button and go back to main activity
        binding.backButton.setOnClickListener {
            onBackPressed()
        }

        // set on click listener for info button and open youtube video
        binding.infoButton.setOnClickListener {
            val url = "https://youtu.be/WtCOpg-JF1s"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }

        // set on click listener for logout button and log out
        binding.logoutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, OnboardingActivity::class.java)
            // clear back stack
            intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
            )
            startActivity(intent)
            finish()
        }

        binding.email.isEnabled = false

        val pickMedia =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                // Callback is invoked after the user selects a media item or closes the photo picker
                if (uri != null) {
                    // If the user selected a picture
                    binding.profilePicture.setPadding(0)
                    binding.profilePicture.setImageURI(uri)
                    selectedPicture = uri
                }
            }

        binding.profilePictureContainer.setOnClickListener {
            pickMedia.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
        }
    }
}
