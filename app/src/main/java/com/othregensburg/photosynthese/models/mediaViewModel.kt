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

class mediaViewModel(application: Application) : AndroidViewModel(application) {
    var storageRef: StorageReference = FirebaseStorage.getInstance().getReference()
    val db = FirebaseFirestore.getInstance()

    //inserts Media into Firestore and Firebase Storage
    fun insert(media: Media)=viewModelScope.launch(Dispatchers.IO){

        //generete new document in Firestore and store id
        val mediaId = db.collection("media").document().id
        media.id = mediaId

        //set reference to path in Firebase
        val reference = "media/${media.event_id}/${mediaId}.jpg"
        media.reference = reference

        //format media object so Uri doesn't get into Firestore
        val uploadMedia= mapOf(
            "id" to media.id,
            "event_id" to media.event_id,
            "reference" to media.reference,
            "timestamp" to media.timestamp,
            "user" to media.user
        )

        //insert formated media into Firestore
        db.collection("media").document(mediaId)
            .set(uploadMedia)

        //insert Uri in Firebase Storage
        storageRef.child("${reference}").putFile(media.content!!)

    }

    //deletes given Media from Firebase
    fun delete(media: Media)=viewModelScope.launch(Dispatchers.IO){

    }

    //gets all Media for given Event from Firebase
    fun getEventMedia(event_id:String?="0"): MutableLiveData<List<Media>> {
        var result:MutableLiveData<List<Media>>  =  MutableLiveData()
        return result
    }
}