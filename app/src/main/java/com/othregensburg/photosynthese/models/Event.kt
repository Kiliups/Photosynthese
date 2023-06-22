package com.othregensburg.photosynthese.models

import android.net.Uri
import java.io.Serializable

data class Event(
    var id: String?,
    var content: Uri?,
    var admins: List<String>?,
    var name: String?,
    var event_date: Long?,
    var start_date: Long?,
    var end_date: Long?,
    var location: String?,
    var participants: List<String>?,
    var picture: String?,
    var description: String?,
    var status: String?
): Serializable



