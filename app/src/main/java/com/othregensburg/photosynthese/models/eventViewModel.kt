package com.othregensburg.photosynthese.models

import android.app.Application
import android.net.Uri
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
import java.util.*

class eventViewModel(application: Application) : AndroidViewModel(application) {

    private val storageRef: StorageReference = FirebaseStorage.getInstance().getReference()
    private val db = FirebaseFirestore.getInstance()

    var events: MutableLiveData<List<Event>> = MutableLiveData()

    //insert given Event into Firebase
    fun insert(event: Event) = viewModelScope.launch(Dispatchers.IO){

        //generate id for firestore
        val eventId = db.collection("event").document().id
        event.id = eventId

        //generate reference to event picture for firebase storage and upload image to storage
        if (event.picture != null) {
            event.reference = "event/${eventId}.jpg"
            storageRef.child(event.reference!!).putFile(event.picture!!).await()
        }

        //create map for firestore
        val uploadEvent = mapOf(
            "name" to event.name,
            "admins" to event.admins,
            "event_date" to event.event_date,
            "start_date" to event.start_date,
            "end_date" to event.end_date,
            "location" to event.location,
            "participants" to event.participants,
            "reference" to event.reference,
            "id" to event.id,
            "description" to event.description,
            "status" to event.status
        )

        //upload event object to firestore
        db.collection("event").document(eventId).set(uploadEvent)

    }

    //delete given Event from Firebase
    fun delete(event: Event) = viewModelScope.launch(Dispatchers.IO) {

        //delete event from firestore
        db.collection("event").document(event.id!!).delete()

        //delete image from firebase storage
        if (event.picture  != null){
            storageRef.child(event.reference!!).delete()
        }
    }

    //get all events by a user
    fun getEventsByUser(uid: String?): MutableLiveData<List<Event>> {

        var result: MutableLiveData<List<Event>> = MutableLiveData()

        //get all media objects from firestore that have the given event_id in right order
        if (uid != null) {
            Log.e("TEST", "uid wasn't null")
            db.collection("event").whereArrayContains("participants", uid).
            get().addOnSuccessListener { documents ->

                Log.d("DOCUMENTS", "empty: ${documents.isEmpty}")

                //create help list
                val eventList = mutableListOf<Event>()

                //start coroutine and wait until all media objects are downloaded
                viewModelScope.launch(Dispatchers.Main) {

                    //for each event item in documents create an event object and add it to help list
                    for (item in documents) {

                       //create event object
                       val event = Event(
                           item.get("admins") as List<String?>,
                           item.get("name") as String?,
                           item.get("event_date") as Long?,
                           item.get("start_date") as Long?,
                           item.get("end_date") as Long?,
                           item.get("location") as String?,
                           item.get("participants") as List<String?>,
                           null as Uri?, // event picture not in database (storage)
                           item.get("reference") as String?,
                           item.get("id") as String?,
                           item.get("description") as String?,
                           item.get("status") as String?
                       )

                       //download event picture uri from firebase storage
                       if(event.reference!=null){
                           val uri = storageRef.child(event.reference!!).downloadUrl.await()
                           event.picture = uri
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

    //set status of event
    private fun setEventStatus(event: Event){

        val currentDate = Date().time
        val start = event.start_date
        val end = event.end_date

        if(currentDate < start!!){
            event.status = "FUTURE"
        }
        else if(currentDate > end!!){
            event.status = "PAST"
        }
        else{
            event.status = "ACTIVE"
        }

    }

    fun sortEventsByStatus(events: List<Event>): List<List<Event>>{

        val futureEvents = mutableListOf<Event>()
        val activeEvents = mutableListOf<Event>()
        val pastEvents = mutableListOf<Event>()

        //sorts each event into a list depending on its status
        for(event in events){
            setEventStatus(event)
            when(event.status){
                "ACTIVE" -> activeEvents.add(event)
                "FUTURE" -> futureEvents.add(event)
                "PAST" -> pastEvents.add(event)
            }
        }

        val sortedEvents = mutableListOf<List<Event>>()
        sortedEvents.add(activeEvents)
        sortedEvents.add(futureEvents)
        sortedEvents.add(pastEvents)

        return sortedEvents
    }





}
