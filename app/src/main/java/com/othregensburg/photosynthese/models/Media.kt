package com.othregensburg.photosynthese.models

import android.net.Uri
import com.google.firebase.Timestamp

data class Media(var id: String?, val event_id:String?, var reference: String?, val timestamp: Long?, val user:String?, var content:Uri?)
