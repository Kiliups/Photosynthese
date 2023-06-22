package com.othregensburg.photosynthese.models

import android.net.Uri
import com.google.firebase.Timestamp
//Data model class for database
data class Media(
    var id: String?,
    val event_id: String?,
    var reference: String?,
    val timestamp: Long?,
    var user: String?,
    var content: Uri?
)

