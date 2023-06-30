package com.othregensburg.photosynthese.models

import android.net.Uri

//Data model class for database
data class Media(
    var id: String?,
    val eventId: String?,
    var reference: String?,
    val timestamp: Long?,
    var user: String?,
    var content: Uri?
)

