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
import com.othregensburg.photosynthese.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale

class userViewModel(application: Application) : AndroidViewModel(application) {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storageRef = FirebaseStorage.getInstance().getReference()
    var isDone = MutableLiveData<Boolean>()
    fun createUser(
        username: String, firstname: String, lastname: String, email: String, password: String
    ) = viewModelScope.launch(Dispatchers.IO) {
        val user = username.lowercase(Locale.getDefault())

        //check if username is already taken
        db.collection("user").whereEqualTo("username", user).get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    // If username is taken, show error
                    Toast.makeText(
                        getApplication(),
                        getApplication<Application>().resources.getString(R.string.username_taken),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // If username is not taken, create user
                    auth.createUserWithEmailAndPassword(
                        email, password
                    ).addOnSuccessListener {
                        // If user created, upload user data to database
                        val userId = it.user!!.uid
                        upload(
                            userId, user, email, firstname, lastname, "user/" + userId, null
                        )
                        Toast.makeText(
                            getApplication(),
                            getApplication<Application>().resources.getString(R.string.user_created),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

    }

    fun login(user: String, password: String): MutableLiveData<Boolean> {
        var email = user
        email = email.lowercase(Locale.getDefault())
        val result = MutableLiveData<Boolean>()
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
                            result.value = it.isSuccessful
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
                result.value = it.isSuccessful
            }
        }
        return result
    }

    fun getUser(id: String): MutableLiveData<User?> {
        val userLive = MutableLiveData<User?>()
        var user: User?
        db.collection("user").document(id).get().addOnSuccessListener {
            viewModelScope.launch(Dispatchers.Main) {
                user = User(
                    it.id as String?,
                    it.get("email") as String?,
                    it.get("username") as String?,
                    it.get("firstname") as String?,
                    it.get("lastname") as String?,
                    it.get("reference") as String?,
                    null
                )
                val reference = user!!.reference
                try {
                    val uri = storageRef.child(reference!!).downloadUrl.await()
                    user!!.picture = uri
                } catch (e: Exception) {
                    user!!.picture = null
                }
                userLive.value = user
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
        val _username = username.lowercase(Locale.getDefault())
        val reference = "user/" + id
        db.collection("user").whereEqualTo("username", _username).get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    // If username is taken, show error
                    if (documents.documents[0].id != id) {
                        Toast.makeText(
                            getApplication(),
                            getApplication<Application>().resources.getString(R.string.username_taken),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        upload(
                            id, _username, email, firstname, lastname, reference, selectedPicture
                        )
                    }
                } else {
                    upload(id, _username, email, firstname, lastname, reference, selectedPicture)
                }
            }
    }

    private fun upload(
        id: String,
        username: String,
        email: String?,
        firstname: String,
        lastname: String,
        reference: String,
        selectedPicture: Uri?
    ) {
        val uploadUser = mapOf(
            "username" to username,
            "firstname" to firstname,
            "lastname" to lastname,
            "email" to email,
            "reference" to reference,
        )
        db.collection("user").document(id).set(uploadUser)
        if (selectedPicture != null) storageRef.child(reference).putFile(selectedPicture)
        isDone.value = true
    }
}