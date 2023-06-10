package com.othregensburg.photosynthese

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.othregensburg.photosynthese.adapter.EventAdapter
import com.othregensburg.photosynthese.models.*


class MainActivity : AppCompatActivity() {

    private lateinit var EventViewModel: eventViewModel

    var eventItemClickListener = object : EventAdapter.eventItemClickListener {
        override fun onItemClicked(event: Event) {
            val intent = Intent(this@MainActivity, EventActivity::class.java)
            startActivity(intent)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //set up recycler views

        val activeEvents: RecyclerView = findViewById(R.id.recyclerView_events_active)
        activeEvents.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val activeAdapter = EventAdapter(emptyList(), "ACTIVE", eventItemClickListener)
        activeEvents.adapter = activeAdapter

        val futureEvents: RecyclerView = findViewById(R.id.recyclerView_events_future)
        futureEvents.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val futureAdapter = EventAdapter(emptyList(), "FUTURE", eventItemClickListener)
        futureEvents.adapter = futureAdapter

        val memoryEvents: RecyclerView = findViewById(R.id.recyclerView_events_memory)
        memoryEvents.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val memoryAdapter = EventAdapter(emptyList(), "MEMORY", eventItemClickListener)
        memoryEvents.adapter = memoryAdapter

        //set up event view model

        EventViewModel = ViewModelProvider(this).get(eventViewModel::class.java)
        var eventLiveData: LiveData<List<Event>> = EventViewModel.getEventsByUser("gudrun")

        eventLiveData.observe(this, androidx.lifecycle.Observer { events ->
            events?.let {

                var sortedEvents = EventViewModel.sortEventsByStatus(events)

                activeAdapter.updateEvents(sortedEvents[0])
                futureAdapter.updateEvents(sortedEvents[1])
                memoryAdapter.updateEvents(sortedEvents[2])
            }
        })
    }
}
