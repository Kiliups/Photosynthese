package com.othregensburg.photosynthese.models

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class eventViewModel(application: Application) : AndroidViewModel(application) {

    private val storageRef: StorageReference = FirebaseStorage.getInstance().getReference()
    private val db = FirebaseFirestore.getInstance()

    //insert given Event into Firebase
    fun insert(event: Event) = viewModelScope.launch(Dispatchers.IO){

        //generate id for firestore
        val eventId = db.collection("event").document().id
        event.id = eventId

        //generate reference to event picture for firebase storage
        val reference = "event/${eventId}.jpg"

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
            "picture" to null

        )

        //upload image to firebase storage
        if(event.content!=null){
            storageRef.child(event.picture!!).putFile(event.content!!).await()
            event.picture = reference
        }

        //upload event object to firestore
        db.collection("event").document(eventId).set(uploadEvent)

    }

    //delete given Event from Firebase
    fun delete(event: Event) = viewModelScope.launch(Dispatchers.IO) {

        //delete event from firestore
        db.collection("event").document(event.id!!).delete()

        //delete image from firebase storage
        if (event.content != null){
            storageRef.child(event.picture!!).delete()
        }
    }

    //get all events by a user
    fun getEventsByUser(username: String?): MutableLiveData<List<Event>> {

        var result: MutableLiveData<List<Event>> = MutableLiveData()

        //get all media objects from firestore that have the given event_id in right order
        if (username != null) {
            db.collection("event").whereArrayContains("participants", username).
            get().addOnSuccessListener { documents ->

                    Log.d("DOCUMENTS", "empty: ${documents.isEmpty}")

                    //create help list
                    val eventList = mutableListOf<Event>()

                    //start coroutine and wait until all media objects are downloaded
                    viewModelScope.launch(Dispatchers.Main) {

                        //for each event item in documents create a event object and add it to help list
                        for (item in documents) {

                            //create event object
                            val event = Event(
                                item.get("id") as String?,
                                null,
                                item.get("admins") as List<String>?,
                                item.get("name") as String?,
                                item.get("event_date") as Long?,
                                item.get("start_date") as Long?,
                                item.get("end_date") as Long?,
                                item.get("location") as String?,
                                item.get("participants") as List<String>?,
                                item.get("picture") as String?
                            )

                            //download event picture uri from firebase storage
                            if(event.picture!=null){
                                val uri = storageRef.child(event.picture!!).downloadUrl.await()
                                event.content = uri
                            }

                            //add media object to help list
                            eventList.add(event)
                        }

                        //set help list as result
                        result.value = eventList
                    }
                }

        }
        return result
    }
}
