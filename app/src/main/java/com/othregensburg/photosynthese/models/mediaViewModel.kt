package com.othregensburg.photosynthese.models

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileOutputStream

class mediaViewModel(application: Application) : AndroidViewModel(application) {

    var storageRef: StorageReference = FirebaseStorage.getInstance().getReference()
    var db = FirebaseFirestore.getInstance()

    //inserts given Media into Firebase
    fun insert(media: Media) = viewModelScope.launch(Dispatchers.IO) {

        //generate id for firestore
        val mediaId = db.collection("media").document().id
        media.id = mediaId

        //generate reference for firebase storage
        val reference = "media/${media.event_id}/${mediaId}.jpg"
        media.reference = reference

        //create map for firestore
        val uploadMedia = mapOf(
            "event_id" to media.event_id,
            "reference" to media.reference,
            "timestamp" to media.timestamp,
            "user" to media.user
        )
        //upload media object to firestore
        db.collection("media").document(mediaId).set(uploadMedia)

        //compress image to 75% quality and 1200x1600px
        val uploadUri=compressImage(media.content!!,75)
        //upload image to firebase storage
        storageRef.child("${reference}").putFile(uploadUri!!).await()

    }

    //deletes given Media from Firebase
    fun delete(media: Media) = viewModelScope.launch(Dispatchers.IO) {

        //delete data from firestore
        db.collection("media").document(media.id!!).delete()

        //delete data from firebase storage
        storageRef.child(media.reference!!).delete()
    }

    //gets all Media from Firebase
    fun getEventMedia(event_id: String? = "0"): MutableLiveData<List<Media>> {

        var result: MutableLiveData<List<Media>> = MutableLiveData()

        //get all media objects from firestore that have the given event_id in right order
        db.collection("media").whereEqualTo("event_id", event_id).orderBy("timestamp")
            .get().addOnSuccessListener { documents ->

                //create help list
                val mediaList = mutableListOf<Media>()

                //start coroutine and wait until all media objects are downloaded
                viewModelScope.launch(Dispatchers.IO) {

                    //for each media object in documents create a media object and add it to help list
                    for (item in documents) {

                        val media = Media(
                            item.id as String?,
                            item.get("event_id") as String?,
                            item.get("reference") as String?,
                            item.get("timestamp") as Long?,
                            item.get("user") as String?,
                            null
                        )

                        //download media uri from firebase storage
                        val uri = storageRef.child(media.reference!!).downloadUrl.await()
                        media.content = uri

                        //add media object to help list
                        mediaList.add(media)
                    }

                    //set help list as result
                    result.value = mediaList
                }
            }
        return result
    }

    private fun compressImage(uri: Uri, quality: Int): Uri {
        //get context
        val context: Context = getApplication<Application>().applicationContext

        //set resolution for image
        val requestOptions = RequestOptions().override(1200, 1600)

        //get bitmap from uri and apply resolution
        val bitmap = Glide.with(context)
            .asBitmap()
            .load(uri)
            .apply(requestOptions)
            .submit()
            .get()

        //create new file in cache directory
        val file = File(context.cacheDir, "${System.currentTimeMillis()}.jpg")
        //write bitmap to file
        val outputStream = FileOutputStream(file)
        //compress bitmap with given quality
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        //close outputStream
        outputStream.close()

        //return uri of file
        return file.toUri()
    }


}