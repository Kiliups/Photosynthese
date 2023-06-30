package com.othregensburg.photosynthese.models

import android.app.Activity
import android.app.Application
import android.content.Intent.getIntent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.othregensburg.photosynthese.MainActivity
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

        event.reference = "event/${eventId}.jpg"

        //generate reference to event picture for firebase storage and upload image to storage
        if (event.picture != null) {
            storageRef.child(event.reference!!).putFile(event.picture!!).await()
        }
        else{
            event.reference = null
        }

        //create map for firestore
        val uploadEvent = mapOf(

            //uri and status not in database

            "admins" to event.admins,
            "name" to event.name,
            "event_date" to event.event_date,
            "start_date" to event.start_date,
            "end_date" to event.end_date,
            "location" to event.location,
            "participants" to event.participants,
            "reference" to event.reference,
            "id" to event.id,
            "description" to event.description
        )

        //upload event object to firestore
        db.collection("event").document(eventId).set(uploadEvent)
    }

    //delete given Event from Firebase
    fun delete(event: Event) = viewModelScope.launch(Dispatchers.IO) {

        //delete event from firestore
        db.collection("event").document(event.id!!).delete()

        //delete image from firebase storage
        if (event.reference  != null){
            storageRef.child(event.reference!!).delete()
        }
    }

    //get all events by a user
    fun getEventsByUser(uid: String?): MutableLiveData<List<Event>> {

        var result: MutableLiveData<List<Event>> = MutableLiveData()

        //get all media objects from firestore that have the given event_id in right order
        if (uid != null) {
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
                           item.get("admins") as MutableList<String?>,
                           item.get("name") as String?,
                           item.get("event_date") as Long?,
                           item.get("start_date") as Long?,
                           item.get("end_date") as Long?,
                           item.get("location") as String?,
                           item.get("participants") as MutableList<String?>,
                           null as Uri?, // event picture not in database (storage)
                           item.get("reference") as String?,
                           item.get("id") as String?,
                           item.get("description") as String?,
                           null as String? // event status not in database (storage)
                       )

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

    fun addUserToEvent(uid: String, event_id: String, activity: AppCompatActivity) {
        db.collection("event")
            .whereEqualTo("id", event_id)
            .get()
            .addOnSuccessListener { documents ->
                if(documents.isEmpty)
                    Toast.makeText(activity, "event couldn't be found", Toast.LENGTH_SHORT).show()
                for (item in documents) {
                    val list = item.get("participants") as MutableList<String?>
                    var notInList = true
                    for (x in list){
                        if (x == uid){
                            Toast.makeText(activity, "already registered", Toast.LENGTH_SHORT).show()
                            notInList = false
                        }
                    }
                    if (notInList){
                        list.add(uid)
                        db.collection("event").document(event_id)
                            .update("participants", list)
                            .addOnSuccessListener {
                                Toast.makeText(activity, "successfully registered", Toast.LENGTH_SHORT).show()
                                activity.recreate()
                            }
                            .addOnFailureListener {
                                Toast.makeText(activity, "error while joining event", Toast.LENGTH_SHORT).show()}
                    }
                }
            }
    }

    suspend fun getUriFromPictureReference(picture: String): Uri {

        var uri: Uri = Uri.EMPTY

        //download event picture uri from firebase storage
        if(picture!=null){
            uri = storageRef.child(picture!!).downloadUrl.await()
        }

        return uri

    }

    fun leaveEvent(uid: String, eventId: String) {
        db.collection("event").document(eventId)
            .get()
            .addOnSuccessListener { document ->
                val list = document.get("participants") as MutableList<String?>
                list.remove(uid)
                db.collection("event").document(eventId)
                    .update("participants", list)
            }
    }
    fun update(event: Event) {
        val updatedEvent = mapOf(
            "admins" to event.admins,
            "name" to event.name,
            "event_date" to event.event_date,
            "start_date" to event.start_date,
            "end_date" to event.end_date,
            "location" to event.location,
            "participants" to event.participants,
            "reference" to event.reference,
            "id" to event.id,
            "description" to event.description
        )
        db.collection("event").document(event.id!!)
            .update(updatedEvent)
    }

}
