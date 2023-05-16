package com.othregensburg.photosynthese.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.othregensburg.photosynthese.R
import com.othregensburg.photosynthese.models.Event

class EventAdapter(private val EventList: List<String>): RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    inner class EventViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        val eventView: TextView = itemView.findViewById(R.id.event_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {

        val adapterLayout = LayoutInflater.from(parent.context).inflate(R.layout.event_list_item, parent, false)
        return EventViewHolder(adapterLayout)
    }

    override fun getItemCount(): Int {

        return EventList.size
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {

        val eventname = EventList[position]
        holder.eventView.text = eventname
    }
}