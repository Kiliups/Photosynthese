package com.othregensburg.photosynthese.models

import android.net.Uri
import com.google.firebase.storage.StorageReference

data class User(
    val id: String?,
    val email: String?,
    val username: String?,
    val firstname: String?,
    val lastname: String?,
    val reference: String?,
    var picture:Uri?
)
