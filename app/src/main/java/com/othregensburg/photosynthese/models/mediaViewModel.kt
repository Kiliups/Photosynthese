package com.othregensburg.photosynthese.models

import android.app.Application
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
    val database = FirebaseFirestore.getInstance()

    //inserts Media into Firestore and Firebase Storage
    fun insert(media: Media)=viewModelScope.launch(Dispatchers.IO){
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