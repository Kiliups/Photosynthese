package com.othregensburg.photosynthese

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.othregensburg.photosynthese.adapter.EventAdapter
import com.othregensburg.photosynthese.models.Event
import com.othregensburg.photosynthese.models.Location
import com.othregensburg.photosynthese.models.eventViewModel
import java.util.*
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private val randomEvents = List(50) {
        Event(
            null,
            null,
            null,
            ("Event" + Random.nextInt('Z' - 'A')).toString(),
            Date().time,
            Random.nextLong(),
            Random.nextLong(),
            Location(90.0, 90.0),
            null,
            null
        )
    }
    fun insertEvent(event: Event) {
        var eVM = eventViewModel(application)
        eVM.insert(event)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //set up recycler views

        val recyclerViewActive: RecyclerView = findViewById(R.id.recyclerView_events_active)
        recyclerViewActive.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewActive.adapter = EventAdapter(randomEvents, "active")

        val recyclerViewFuture: RecyclerView = findViewById(R.id.recyclerView_events_future)
        recyclerViewFuture.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewFuture.adapter = EventAdapter(randomEvents, "future")

        val recyclerViewMemory: RecyclerView = findViewById(R.id.recyclerView_events_memory)
        recyclerViewMemory.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewMemory.adapter = EventAdapter(randomEvents, "memory")

        var add: ImageButton = findViewById(R.id.icon_add)
        var position = 0
        add.setOnClickListener {
            var event = randomEvents[position]
            insertEvent(event)
            position++
            if (position == randomEvents.size) position = 0
        }

    }
}
