package com.othregensburg.photosynthese

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.setPadding
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.othregensburg.photosynthese.databinding.ActivityProfileBinding
import com.othregensburg.photosynthese.models.User
import com.othregensburg.photosynthese.models.userViewModel
import com.othregensburg.photosynthese.onboarding.OnboardingActivity

class ProfileActivity : AppCompatActivity() {
    lateinit var binding: ActivityProfileBinding
    val auth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val uVM = ViewModelProvider(this).get(userViewModel::class.java)
        var currentUser:User?=null
        uVM.getUser(auth.currentUser!!.uid).observe(this) { user ->
            if (user != null) {
                binding.username.setText(user.username)
                binding.firstName.setText(user.firstname)
                binding.lastName.setText(user.lastname)
                binding.email.setText(user.email)
                currentUser=user
            }
        }

        binding.backButton.setOnClickListener {
            onBackPressed()
        }
        binding.infoButton.setOnClickListener {
            val url = "https://youtu.be/WtCOpg-JF1s" // Der gewünschte Link hier
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }
        binding.logoutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, OnboardingActivity::class.java)
            startActivity(intent)
            finish()
        }
        var selectedPicture: Uri? = null;
        binding.checkButton.setOnClickListener {
            /*uVM.updateUser(
                auth.currentUser!!.uid,
                binding.username.text.toString(),
                currentUser!!.email,
                binding.firstName.text.toString(),
                binding.lastName.text.toString(),
                selectedPicture
            )*/
        }
        val pickMedia =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                // Callback is invoked after the user selects a media item or closes the photo picker
                if (uri != null) {
                    //If the user selected a picture
                    binding.profilePicture.setPadding(0)
                    binding.profilePicture.setImageURI(uri)
                    selectedPicture = uri
                } else {
                    //If no picture was selected
                }
            }
        binding.profilePictureContainer.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        })
    }
}