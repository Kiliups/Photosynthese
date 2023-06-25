package com.othregensburg.photosynthese.models

import android.net.Uri
import java.io.Serializable

data class Event(
    var admins: MutableList<String?>,
    var name: String?,
    var event_date: Long?,
    var start_date: Long?,
    var end_date: Long?,
    var location: String?,
    var participants: MutableList<String?>,
    var picture: Uri?,
    var reference: String?,
    var id: String?,
    var description: String?,
    var status: String?
): Serializable



