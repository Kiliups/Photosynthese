package com.othregensburg.photosynthese.models

import android.app.Application
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class mediaViewModel(application: Application) : AndroidViewModel(application) {
    private var storageRef: StorageReference = FirebaseStorage.getInstance().getReference()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    val isLoading = MutableLiveData<Boolean>()
    var isDone = MutableLiveData<Boolean>()

    // inserts given Media into Firebase
    fun insert(media: Media) = viewModelScope.launch(Dispatchers.IO) {
        withContext(Dispatchers.Main) {
            isLoading.value = true
        }
        // generate id for firestore
        val mediaId = db.collection("media").document().id
        media.id = mediaId

        // get file type
        var type = media.content.toString()
        type = type.substring(type.lastIndexOf(".") + 1)

        // set user id
        if (auth.currentUser != null) media.user = auth.currentUser!!.uid

        // generate reference for firebase storage
        val reference = "media/${media.eventId}/$mediaId.$type"
        media.reference = reference

        // create map for firestore
        val uploadMedia = mapOf(
            "event_id" to media.eventId,
            "reference" to media.reference,
            "timestamp" to media.timestamp,
            "user" to media.user
        )
        // upload media object to firestore
        db.collection("media").document(mediaId).set(uploadMedia).addOnSuccessListener {
            Log.e("firebase", "upload success")
        }.addOnFailureListener {
            Log.e("firebase", it.message.toString())
        }

        // compress image to 75% quality and 1200x1600px
        val uploadUri = compressMedia(media.content!!, 75, type)

        // upload image to firebase storage
        storageRef.child(reference).putFile(uploadUri).await()

        withContext(Dispatchers.Main) {
            isLoading.value = false
        }
    }

    // deletes given Media from Firebase
    fun delete(media: Media) = viewModelScope.launch(Dispatchers.Main) {
        // delete data from firestore
        db.collection("media").document(media.id!!).delete()

        // delete data from firebase storage
        storageRef.child(media.reference!!).delete()
    }

    // gets all Media from Firebase
    fun getEventMedia(eventId: String? = "0"): MutableLiveData<MutableList<Media>> {
        val result: MutableLiveData<MutableList<Media>> = MutableLiveData()
        isLoading.value = true

        // get all media objects from firestore that have the given event_id in right order
        db.collection("media").whereEqualTo("event_id", eventId).orderBy("timestamp").get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    result.value = null
                } else {
                    // create help list
                    val mediaList = mutableListOf<Media>()

                    // start coroutine and wait until all media objects are downloaded
                    viewModelScope.launch(Dispatchers.Main) {
                        // for each media object in documents create a media object and add it to help list
                        for (item in documents) {
                            val media = Media(
                                item.id as String?,
                                item.get("event_id") as String?,
                                item.get("reference") as String?,
                                item.get("timestamp") as Long?,
                                item.get("user") as String?,
                                null
                            )

                            // download media uri from firebase storage
                            val uri = storageRef.child(media.reference!!).downloadUrl.await()
                            media.content = uri

                            // add media object to help list
                            mediaList.add(media)
                            // set help list as result
                            result.value = mediaList
                        }
                    }
                }
            }
        // set isLoading to false
        isLoading.value = false
        return result
    }

    private fun compressMedia(uri: Uri, quality: Int, type: String): Uri {
        var result: Uri? = null
        // get context
        val context: Context = getApplication<Application>().applicationContext

        // check if media is image or video
        if (type == "jpg") {
            // set resolution for image
            val requestOptions = RequestOptions().override(1200, 1600)

            // get bitmap from uri and apply resolution
            val bitmap =
                Glide.with(context).asBitmap().load(uri).apply(requestOptions).submit().get()
            // create new file in cache directory
            val file = File(context.cacheDir, "${System.currentTimeMillis()}.jpg")
            // write bitmap to file
            val outputStream = FileOutputStream(file)
            // compress bitmap with given quality
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            // close outputStream
            outputStream.close()
            result = file.toUri()
        }

        // check if media is video
        if (type == "mp4") {
            result = uri
        }

        // return uri of file
        return result!!
    }

    fun saveMedia(media: Media) = viewModelScope.launch(Dispatchers.IO) {
        val context = getApplication<Application>().applicationContext
        val fileName = media.reference
        val resolver: ContentResolver = context.contentResolver
        val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(
                MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Photosynthese"
            ) // Specify the folder here
        }
        var outputStream: OutputStream? = null

        try {
            val uri = resolver.insert(contentUri, contentValues)
            if (uri != null) {
                outputStream = resolver.openOutputStream(uri)
                outputStream?.use { stream ->
                    val bitmap = Glide.with(context).asBitmap().load(media.content).submit().get()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                    stream.flush()
                }
                withContext(Dispatchers.Main) {
                    isDone.value = true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                isDone.value = false
            }
        } finally {
            outputStream?.close()
        }
    }
}
