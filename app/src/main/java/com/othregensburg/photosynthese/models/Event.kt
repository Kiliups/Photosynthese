package com.othregensburg.photosynthese.models

import android.net.Uri

data class Event(
    var admins: List<String>?,
    var name: String?,
    var event_date: Long?,
    var start_date: Long?,
    var end_date: Long?,
    var location: String?,
    var participants: List<String?>,
    var picture: Uri?,
    var reference: String?,
    var id: String?,
    var description: String?
)
