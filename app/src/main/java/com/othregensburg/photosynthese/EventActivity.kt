package com.othregensburg.photosynthese

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.othregensburg.photosynthese.R
import com.othregensburg.photosynthese.models.Event

class EventActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event)

        // Get the event from the intent
        val event = intent.getSerializableExtra("event") as Event

        // Get the status of the event
        val status = event.status

        // Replace the fragment based on the status
        if(status == "ACTIVE")
            replaceFragment(EventCameraFragment())
        else if(status == "FUTURE")
            replaceFragment(EventDetailFragment.newInstance(event))
        else if(status == "MEMORY")
            replaceFragment(EventGalleryFragment())
    }

    // Replaces the current fragment with the given fragment
    fun replaceFragment(fragment: Fragment) {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.commit()
    }
}