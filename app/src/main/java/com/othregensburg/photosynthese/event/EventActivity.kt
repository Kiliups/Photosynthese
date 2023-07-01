package com.othregensburg.photosynthese.event

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.othregensburg.photosynthese.R
import com.othregensburg.photosynthese.models.Event

class EventActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event)

        // get the event from the intent
        val event = intent.getSerializableExtra("event") as Event

        // replace the fragment based on the status
        when (event.status) {
            "ACTIVE" -> replaceFragment(EventCameraFragment.newInstance(event))
            "FUTURE" -> replaceFragment(EventDetailFragment.newInstance(event))
            "MEMORY" -> replaceFragment(EventGalleryFragment.newInstance(event))
        }
    }

    // replaces the current fragment with the given fragment
    private fun replaceFragment(fragment: Fragment) {

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.commit()
    }
}
