package com.othregensburg.photosynthese.models

data class Event(
    var admins: List<String>?,
    var name: String?,
    var event_date: Long?,
    var start_date: Long?,
    var end_date: Long?,
    var location: LongArray?,
    var participants: List<String>?,
    var picture: String?
)
