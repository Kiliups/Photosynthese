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

class mediaViewModel(application: Application) : AndroidViewModel(application) {
    var storageRef: StorageReference = FirebaseStorage.getInstance().getReference()
    val db = FirebaseFirestore.getInstance()

    //inserts media into firestore and firebase Storage
    fun insert(media: Media) = viewModelScope.launch(Dispatchers.IO) {

        //generete new document in firestore and store id
        val mediaId = db.collection("media").document().id
        media.id = mediaId

        //set reference to path in firebase
        val reference = "media/${media.event_id}/${mediaId}.jpg"
        media.reference = reference

        //format media object so uri doesn't get into firestore
        val uploadMedia = mapOf(
            "event_id" to media.event_id,
            "reference" to media.reference,
            "timestamp" to media.timestamp,
            "user" to media.user
        )

        //insert formated media into firestore
        db.collection("media").document(mediaId)
            .set(uploadMedia)

        //insert Uri in firebase storage
        storageRef.child("${reference}").putFile(media.content!!)

    }

    //deletes given Media from Firebase
    fun delete(media: Media) = viewModelScope.launch(Dispatchers.IO) {

        //delete data from firestore
        db.collection("media").document(media.id!!).delete()

        //delete data from firebase storage
        storageRef.child(media.reference!!).delete()
    }

    //gets all media for given event from firebase
    fun getEventMedia(event_id: String? = "0"): MutableLiveData<List<Media>> {

        var result: MutableLiveData<List<Media>> = MutableLiveData()

        //get media from firebase for given event_id in chronological order
        db.collection("media").whereEqualTo("event_id", event_id).orderBy("timestamp")
            .get().addOnSuccessListener { documents ->

                //help list to store media objects into this List
                val mediaList = mutableListOf<Media>()

                //launch new Coroutine that all result are in right order
                viewModelScope.launch(Dispatchers.IO) {

                    //create new media object for each document returned by Firebase
                    for (item in documents) {

                        val media = Media(
                            item.id as String?,
                            item.get("event_id") as String?,
                            item.get("reference") as String?,
                            item.get("timestamp") as Long?,
                            item.get("user") as String?,
                            null
                        )

                        //get picture uri for each media object from firebase storage and wait until it is downloaded
                        val uri = storageRef.child(media.reference!!).downloadUrl.await()
                        media.content = uri

                        //after media uri is downloaded add media object to help list
                        mediaList.add(media)
                    }

                    //after ever media object is downloaded set live data equal to help list
                    result.value = mediaList
                }
            }

        return result
    }
}