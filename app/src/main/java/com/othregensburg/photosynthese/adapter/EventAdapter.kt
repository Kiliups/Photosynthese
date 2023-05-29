package com.othregensburg.photosynthese.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.othregensburg.photosynthese.R
import com.othregensburg.photosynthese.models.Event
import java.text.SimpleDateFormat
import java.util.*

class EventAdapter(private val EventList: List<Event>, private val groupid: String): RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    inner class EventViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        val eventTitle: TextView = itemView.findViewById(R.id.event_title)
        val eventDate: TextView = itemView.findViewById(R.id.event_date)
        val eventCard: CardView = itemView.findViewById(R.id.card)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {

        val adapterLayout = LayoutInflater.from(parent.context).inflate(R.layout.event_list_item, parent, false)
        return EventViewHolder(adapterLayout)
    }

    override fun getItemCount(): Int {

        return EventList.size
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {

        holder.eventTitle.text = EventList[position].name

        //format date
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val formattedDate = dateFormat.format(EventList[position].event_date)
        holder.eventDate.text = formattedDate

        //increase size of active events

        if (groupid == "active") {

            val layoutParams = holder.eventCard.layoutParams as ViewGroup.LayoutParams
            layoutParams.height = 624
            layoutParams.width = 624

            holder.eventCard.layoutParams = layoutParams
        }

    }
}