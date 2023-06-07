package com.othregensburg.photosynthese.models

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
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

class userViewModel(application: Application) : AndroidViewModel(application) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    fun createUser(
        username: String, firstname: String, lastname: String, email: String, password: String
    ) = viewModelScope.launch(Dispatchers.IO) {
        val user = username.toLowerCase()
        db.collection("user").whereEqualTo("username", user).get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    Toast.makeText(
                        getApplication(), "Username already taken", Toast.LENGTH_SHORT
                    ).show()
                } else {
                    auth.createUserWithEmailAndPassword(
                        email, password
                    ).addOnSuccessListener {
                        val userId = it.user!!.uid
                        val uploadUser = mapOf(
                            "username" to user,
                            "firstname" to firstname,
                            "lastname" to lastname,
                            "email" to email,
                        )
                        db.collection("user").document(userId).set(uploadUser)
                        Toast.makeText(
                            getApplication(), "User created", Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

    }

    fun login(user: String, password: String) {
        var email = user
        email = email.toLowerCase()
        if (!Patterns.EMAIL_ADDRESS.matcher(user).matches()) {
            db.collection("user").whereEqualTo("username", user).get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty()) {
                        email = documents.documents[0].get("email").toString()
                        Log.d("firebase", "E-Mail-Adresse: $email")
                        auth.signInWithEmailAndPassword(
                            email, password
                        ).addOnCompleteListener {
                            if (it.isSuccessful) {
                                //start main activity
                                Toast.makeText(
                                    getApplication(), "Login successful", Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }else{
                        Toast.makeText(
                            getApplication(), "Login failed", Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        } else {
            auth.signInWithEmailAndPassword(
                email, password
            ).addOnCompleteListener {
                if (it.isSuccessful) {
                    //start main activity
                    Toast.makeText(
                        getApplication(), "Login successful", Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        getApplication(), "Login failed", Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    fun signOut() {
        auth.signOut()
        Toast.makeText(
            getApplication(), "Sign out", Toast.LENGTH_SHORT
        ).show()
    }
}