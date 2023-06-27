package com.othregensburg.photosynthese.models

import android.app.Application
import android.net.Uri
import android.util.Patterns
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class userViewModel(application: Application) : AndroidViewModel(application) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val storageRef = FirebaseStorage.getInstance().getReference()
    fun createUser(
        username: String, firstname: String, lastname: String, email: String, password: String
    ) = viewModelScope.launch(Dispatchers.IO) {
        val user = username.toLowerCase()
        // Check if username is already taken
        db.collection("user").whereEqualTo("username", user).get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    // If username is taken, show error
                    Toast.makeText(
                        getApplication(), "Username already taken", Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // If username is not taken, create user
                    auth.createUserWithEmailAndPassword(
                        email, password
                    ).addOnSuccessListener {
                        val userId = it.user!!.uid
                        val uploadUser = mapOf(
                            "username" to user,
                            "firstname" to firstname,
                            "lastname" to lastname,
                            "email" to email,
                            "reference" to "user/" + userId,
                        )
                        db.collection("user").document(userId).set(uploadUser)
                        Toast.makeText(
                            getApplication(), "User created", Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

    }

    fun login(user: String, password: String): MutableLiveData<Boolean> {
        var email = user
        email = email.toLowerCase()
        var result = MutableLiveData<Boolean>()
        // Check if user is trying to login with email or username
        if (!Patterns.EMAIL_ADDRESS.matcher(user).matches()) {
            // If username, get email from database
            db.collection("user").whereEqualTo("username", user).get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty()) {
                        // If username exists, get email and login
                        email = documents.documents[0].get("email").toString()
                        auth.signInWithEmailAndPassword(
                            email, password
                        ).addOnCompleteListener {
                            if (it.isSuccessful) {
                                // If login successful, set result to true
                                result.value = true
                            } else {
                                // If login unsuccessful, set result to false
                                result.value = false
                            }
                        }
                    } else {
                        // If username does not exist, set result to false
                        result.value = false
                    }
                }
        } else {
            // If email, login
            auth.signInWithEmailAndPassword(
                email, password
            ).addOnCompleteListener {
                if (it.isSuccessful) {
                    // If login successful, set result to true
                    result.value = true
                } else {
                    // If login unsuccessful, set result to false
                    result.value = false
                }
            }
        }
        return result
    }

    fun getUser(id: String): MutableLiveData<User?> {
        var userLive = MutableLiveData<User?>()
        var user :User?=null
        db.collection("user").document(id).get().addOnSuccessListener {
            viewModelScope.launch(Dispatchers.Main) {
                user= User(
                    it.id as String?,
                    it.get("email") as String?,
                    it.get("username") as String?,
                    it.get("firstname") as String?,
                    it.get("lastname") as String?,
                    it.get("reference") as String?,
                    null
                )
                val reference = user!!.reference
                val uri=storageRef.child(reference!!).downloadUrl.await()
                user!!.picture=uri
                userLive.value=user
            }
        }
        return userLive
    }

    fun updateUser(
        id: String,
        username: String,
        email: String?,
        firstname: String,
        lastname: String,
        selectedPicture: Uri?
    ) {
        val reference = "user/" + id
        db.collection("user").whereEqualTo("username", username).get()
            .addOnSuccessListener { documents ->
                if (documents.documents[0].id != id) {
                    // If username is taken, show error
                    Toast.makeText(
                        getApplication(), "Username already taken", Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val uploadUser = mapOf(
                        "username" to username,
                        "firstname" to firstname,
                        "lastname" to lastname,
                        "email" to email,
                        "reference" to reference,
                    )
                    db.collection("user").document(id).set(uploadUser)
                    if (selectedPicture != null)
                        storageRef.child("${reference}").putFile(selectedPicture!!)
                }
            }
    }
}