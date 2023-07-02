package com.othregensburg.photosynthese.models

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.othregensburg.photosynthese.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

class eventViewModel(application: Application) : AndroidViewModel(application) {

    private val storageRef: StorageReference = FirebaseStorage.getInstance().reference
    private val db = FirebaseFirestore.getInstance()

    // insert given Event into database
    fun insert(event: Event) = viewModelScope.launch(Dispatchers.IO) {
        // generate id for firestore
        val eventId = db.collection("event").document().id
        event.id = eventId

        // generate reference to event picture for firebase storage
        event.reference = "event/$eventId.jpg"

        // upload event picture to firebase storage
        if (event.picture != null) {
            storageRef.child(event.reference!!).putFile(event.picture!!).await()
        } else {
            event.reference = null
        }

        // create map for firestore
        val uploadEvent = mapOf(

            // uri and status not in database

            "admins" to event.admins,
            "name" to event.name,
            "event_date" to event.eventDate,
            "start_date" to event.startDate,
            "end_date" to event.endDate,
            "location" to event.location,
            "participants" to event.participants,
            "reference" to event.reference,
            "id" to event.id,
            "description" to event.description
        )

        // upload event object to firestore
        db.collection("event").document(eventId).set(uploadEvent)
    }

    // delete given event from firestore
    fun delete(event: Event) = viewModelScope.launch(Dispatchers.IO) {
        // delete event from firestore
        db.collection("event").document(event.id!!).delete()

        // delete image from firebase storage
        if (event.reference != null) {
            storageRef.child(event.reference!!).delete()
        }
    }

    // get all events by a user
    fun getEventsByUser(uid: String?): MutableLiveData<List<Event>> {
        val result: MutableLiveData<List<Event>> = MutableLiveData()

        // get all media objects from firestore that have the given user id in right order
        if (uid != null) {
            db.collection("event")
                .whereArrayContains("participants", uid)
                .get()
                .addOnSuccessListener { documents ->
                    val eventList = mutableListOf<Event>()

                    // for each event item in documents create an event object and add it to event list
                    viewModelScope.launch(Dispatchers.Main) {
                        for (item in documents) {
                           // create event object
                           val event = Event(
                               item.get("admins") as MutableList<String?>,
                               item.get("name") as String?,
                               item.get("event_date") as Long?,
                               item.get("start_date") as Long?,
                               item.get("end_date") as Long?,
                               item.get("location") as String?,
                               item.get("participants") as MutableList<String?>,
                               null as Uri?, // event picture not in database
                               item.get("reference") as String?,
                               item.get("id") as String?,
                               item.get("description") as String?,
                               null as String? // event status not in database
                           )

                           // add event object to event list
                           eventList.add(event)
                        }

                        result.value = eventList
                    }
            }
        }
        return result
    }

    // set status of event
    private fun setEventStatus(event: Event) {
        val currentDate = Date().time
        val start = event.startDate
        val end = event.endDate

        if (currentDate < start!!) {
            event.status = "FUTURE"
        } else if (currentDate > end!!) {
            event.status = "MEMORY"
        } else {
            event.status = "ACTIVE"
        }
    }

    // sorts events by status
    fun sortEventsByStatus(events: List<Event>): List<List<Event>> {
        val futureEvents = mutableListOf<Event>()
        val activeEvents = mutableListOf<Event>()
        val memoryEvents = mutableListOf<Event>()

        // sorts each event into a list depending on its status
        for (event in events) {
            setEventStatus(event)
            when (event.status) {
                "ACTIVE" -> activeEvents.add(event)
                "FUTURE" -> futureEvents.add(event)
                "MEMORY" -> memoryEvents.add(event)
            }
        }

        val sortedEvents = mutableListOf<List<Event>>()
        sortedEvents.add(activeEvents)
        sortedEvents.add(futureEvents)
        sortedEvents.add(memoryEvents)

        return sortedEvents
    }

    // add user to event
    fun addUserToEvent(uid: String, eventId: String, activity: AppCompatActivity) {
        db.collection("event")
            .whereEqualTo("id", eventId)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(activity, R.string.toast_event_not_fount, Toast.LENGTH_SHORT).show()
                }

                for (item in documents) {
                    val list = item.get("participants") as MutableList<String?>
                    var notInList = true

                    // check if user is already in list
                    for (x in list) {
                        if (x == uid) {
                            Toast.makeText(activity, R.string.toast_already_registerd, Toast.LENGTH_SHORT).show()
                            notInList = false
                        }
                    }

                    // add user to list
                    if (notInList) {
                        list.add(uid)
                        db.collection("event").document(eventId)
                            .update("participants", list)
                            .addOnSuccessListener {
                                Toast.makeText(activity, R.string.toast_successfully_registered, Toast.LENGTH_SHORT).show()
                                activity.recreate()
                            }
                            .addOnFailureListener {
                                Toast.makeText(activity, R.string.toast_error_joining, Toast.LENGTH_SHORT).show()}
                    }
                }
            }
    }

    suspend fun getUriFromPictureReference(picture: String): Uri {
        // download event picture uri from firebase storage
        return storageRef.child(picture).downloadUrl.await()
    }

    // remove user from event
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

    // update event
    fun update(event: Event) {
        // create map for firestore
        val updatedEvent = mapOf(
            "admins" to event.admins,
            "name" to event.name,
            "event_date" to event.eventDate,
            "start_date" to event.startDate,
            "end_date" to event.endDate,
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
