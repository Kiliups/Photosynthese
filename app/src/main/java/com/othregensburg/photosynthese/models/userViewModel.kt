package com.othregensburg.photosynthese.models

import android.app.Application
import android.content.Intent
import android.util.Patterns
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.othregensburg.photosynthese.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class userViewModel(application: Application) : AndroidViewModel(application) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    fun createUser(
        username: String, firstname: String, lastname: String, email: String, password: String
    ) = viewModelScope.launch(Dispatchers.IO) {
        val user = username.toLowerCase()
        db.collection("users").whereEqualTo("username", user).get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    Toast.makeText(
                        getApplication(), "Username already taken", Toast.LENGTH_SHORT
                    ).show()
                } else {
                    auth.createUserWithEmailAndPassword(
                        email, password
                    )
                    val user = auth.currentUser
                    val userId = user!!.uid
                    val uploadUser = mapOf(
                        "username" to user,
                        "firstname" to firstname,
                        "lastname" to lastname,
                        "email" to email,
                    )
                    db.collection("users").document(userId).set(uploadUser)
                    Toast.makeText(
                        getApplication(), "User created", Toast.LENGTH_SHORT
                    ).show()
                }
            }

    }

    fun login(user: String, password: String) {
        var email = user
        if (!Patterns.EMAIL_ADDRESS.matcher(user).matches()) {
            db.collection("users").whereEqualTo("username", user).get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        email = documents.documents[0].get("email").toString()
                    }
                }
        }
        auth.signInWithEmailAndPassword(
            email, password
        ).addOnCompleteListener {
            if (it.isSuccessful) {
                //start main activity
                val intent = Intent(getApplication(), MainActivity::class.java)
                startActivity(getApplication(), intent, null)
            } else {
                //show error
                Toast.makeText(
                    getApplication(), "Login failed", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}