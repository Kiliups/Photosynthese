package com.othregensburg.photosynthese

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.ktx.Firebase
import com.othregensburg.photosynthese.adapter.EventAdapter
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private val randomNames = List(50) {
        ("Event" + Random.nextInt('Z' - 'A')).toString()
    }
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerViewActive: RecyclerView = findViewById(R.id.recyclerView_events_active)
        recyclerViewActive.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewActive.adapter = EventAdapter(randomNames, "active")

        val recyclerViewFuture: RecyclerView = findViewById(R.id.recyclerView_events_future)
        recyclerViewFuture.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewFuture.adapter = EventAdapter(randomNames, "future")

        val recyclerViewMemory: RecyclerView = findViewById(R.id.recyclerView_events_memory)
        recyclerViewMemory.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerViewMemory.adapter = EventAdapter(randomNames, "memory")

    }
}
