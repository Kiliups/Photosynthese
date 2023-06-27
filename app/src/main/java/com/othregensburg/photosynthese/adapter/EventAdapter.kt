package com.othregensburg.photosynthese.adapter

import android.app.Dialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.othregensburg.photosynthese.R
import com.othregensburg.photosynthese.models.Event
import java.text.SimpleDateFormat
import java.util.*


class EventAdapter(private var events: List<Event>, private val status: String, val listener: eventItemClickListener): RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    interface eventItemClickListener {
        fun onItemClicked(event: Event)
        fun onItemSettingsClicked(event: Event, holder: EventViewHolder)
        fun showCopyIdDialog(event_id: String)
        fun showEventPopupMenu(event: Event, view: View)
        fun leaveEvent(event_id: String)
        fun deleteEvent(event: Event)
        fun showChangeTimeTableDialog(event: Event)
        fun formatTimestamp(timestamp: Long): String {
            val timeFormat = SimpleDateFormat("dd.MM.yyyy HH:mm")
            return timeFormat.format(Date(timestamp))
        }
    }


    //update events
    fun updateEvents(updatedEvents: List<Event>) {
        events = updatedEvents
        notifyDataSetChanged()
    }

    inner class EventViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        val eventTitle: TextView = itemView.findViewById(R.id.event_title)
        val eventDate: TextView = itemView.findViewById(R.id.event_date)
        val eventCard: CardView = itemView.findViewById(R.id.card)
        val eventSettings: ImageButton = itemView.findViewById(R.id.event_settings)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {

        val adapterLayout = LayoutInflater.from(parent.context).inflate(R.layout.event_list_item, parent, false)
        return EventViewHolder(adapterLayout)
    }

    override fun getItemCount(): Int {

        return events.size
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {

        val currentEvent = events[position]

        holder.eventTitle.text = currentEvent.name

        //format date
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val formattedDate = currentEvent.event_date?.let { dateFormat.format(it) }
        holder.eventDate.text = formattedDate

        //resize event card for active events
        if (status == "ACTIVE") {
            resizeEventCard(holder, 624)
        }

        holder.eventCard.setOnClickListener {
            listener.onItemClicked(currentEvent)
        }

        holder.eventSettings.setOnClickListener {
            listener.onItemSettingsClicked(currentEvent, holder)
        }
    }

    //resize event card
    private fun resizeEventCard(holder: EventViewHolder,size: Int) {

        val layoutParams = holder.eventCard.layoutParams as ViewGroup.LayoutParams
        layoutParams.height = size
        layoutParams.width = size

        holder.eventCard.layoutParams = layoutParams
    }
}