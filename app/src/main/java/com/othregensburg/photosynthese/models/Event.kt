package com.othregensburg.photosynthese.models

import android.net.Uri
import java.io.Serializable

data class Event(

    var admins: MutableList<String?>,
    var name: String?,
    var eventDate: Long?,
    var startDate: Long?,
    var endDate: Long?,
    var location: String?,
    var participants: MutableList<String?>,
    var picture: Uri?,
    var reference: String?,
    var id: String?,
    var description: String?,
    var status: String?

): Serializable{
    companion object {
        //serialVersionUID is used to ensure that the serialized and deserialized objects are compatible
        private const val serialVersionUID: Long = 1L
    }
}



