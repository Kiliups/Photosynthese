package com.othregensburg.photosynthese.models

import android.app.Application
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class eventViewModel(application: Application) : AndroidViewModel(application) {

    val storageRef: StorageReference = FirebaseStorage.getInstance().getReference()
    val db = FirebaseFirestore.getInstance()

    //insert given Event into Firebase
    fun insert(event: Event) = viewModelScope.launch(Dispatchers.IO){

        //generate id for firestore
        val eventId = db.collection("event").document().id
        event.id = eventId

        //generate reference to event picture for firebase storage
        val reference = "event/${eventId}.jpg"
        event.picture = reference

        //create map for firestore
        val uploadEvent = mapOf(
            "id" to event.id,
            "admins" to event.admins,
            "name" to event.name,
            "event_date" to event.event_date,
            "start_date" to event.start_date,
            "end_date" to event.end_date,
            "location" to event.location,
            "participants" to event.participants,
            "picture" to event.picture
        )
        //upload event object to firestore
        db.collection("event").document(eventId).set(uploadEvent)

        //upload image to firebase storage
        if(event.content!=null){
            storageRef.child(reference).putFile(event.content!!).await()
        }

    }


}