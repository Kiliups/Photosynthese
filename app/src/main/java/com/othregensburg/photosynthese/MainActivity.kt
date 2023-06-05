package com.othregensburg.photosynthese

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.othregensburg.photosynthese.adapter.EventAdapter
import com.othregensburg.photosynthese.models.*

import java.util.*
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private val randomEvents = List(50) {
        Event(
            null,
            null,
            null,
            ("Event" + Random.nextInt('Z' - 'A')),
            Date().time,
            Random.nextLong(),
            Random.nextLong(),
            "Regensburg",
            listOf("user1", "user2", "gudrun"),
            null
        )
    }

    private val toDel = Event(
        "lXqBU9ZIlnEXprQ2N7jU",
        null,
        null,
        "delete me",
        null,
        null,
        null,
        null,
        null,
        null
    )

    fun insertEvent(event: Event) {
        var eVM = eventViewModel(application)
        eVM.insert(event)
    }
    fun deleteEvent(event: Event) {
        var eVM = eventViewModel(application)
        eVM.delete(event)
    }


    private lateinit var EventViewModel: eventViewModel
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //set up recycler views

        val activeEvents: RecyclerView = findViewById(R.id.recyclerView_events_active)
        activeEvents.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val activeAdapter = EventAdapter(emptyList(), "active")
        activeEvents.adapter = activeAdapter

        val futureEvents: RecyclerView = findViewById(R.id.recyclerView_events_future)
        futureEvents.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val futureAdapter = EventAdapter(emptyList(), "future")
        futureEvents.adapter = futureAdapter

        val memoryEvents: RecyclerView = findViewById(R.id.recyclerView_events_memory)
        memoryEvents.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val memoryAdapter = EventAdapter(emptyList(), "memory")
        memoryEvents.adapter = memoryAdapter

        //set up event view model

        EventViewModel = ViewModelProvider(this).get(eventViewModel::class.java)
        var eventLiveData: LiveData<List<Event>> = EventViewModel.getEventsByUser("gudrun")

        eventLiveData.observe(this, androidx.lifecycle.Observer { events ->
            events?.let {
                activeAdapter.updateEvents(events)
                futureAdapter.updateEvents(events)
                memoryAdapter.updateEvents(events)
            }
        })

        //set up add button

        var add: ImageButton = findViewById(R.id.icon_add)
        var position = 0
        add.setOnClickListener {
            var event = randomEvents[position]
            insertEvent(event)
            position++
            if (position == randomEvents.size) position = 0
        }

        //set up profile button as remove button for test purposes

        var profile: ImageButton = findViewById(R.id.icon_account)
        profile.setOnClickListener {
            deleteEvent(toDel)
        }

    }

}
