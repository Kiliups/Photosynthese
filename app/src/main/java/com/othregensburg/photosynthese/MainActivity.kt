package com.othregensburg.photosynthese

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.ktx.Firebase
import com.othregensburg.photosynthese.adapter.EventAdapter
import com.othregensburg.photosynthese.models.Event
import java.util.*
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private val randomEvents = List(50) {
        Event(
            null,
            ("Event" + Random.nextInt('Z' - 'A')).toString(),
            Date().time,
            Random.nextLong(),
            Random.nextLong(),
            LongArray(2) { Random.nextLong() },
            null,
            null
        )
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

    }
}
