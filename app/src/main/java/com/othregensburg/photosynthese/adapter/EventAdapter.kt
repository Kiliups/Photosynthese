package com.othregensburg.photosynthese.adapter

import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.othregensburg.photosynthese.R
import com.othregensburg.photosynthese.models.Event
import com.othregensburg.photosynthese.models.eventViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class EventAdapter(
    private var events: List<Event>,
    private val status: String,
    private val listener: eventItemClickListener
    ) : RecyclerView.Adapter<EventAdapter.EventViewHolder>() {
    interface eventItemClickListener {
        fun onItemClicked(event: Event)
        fun onItemSettingsClicked(event: Event, holder: EventViewHolder)
        fun showCopyIdDialog(eventId: String)
        fun showEventPopupMenu(event: Event, view: View)
        fun leaveEvent(eventId: String)
        fun deleteEvent(event: Event)
        fun showChangeTimeTableDialog(event: Event)
        fun parseTime(timeString: String): Long {
            val timeFormat = SimpleDateFormat("dd.MM.yyyy HH:mm")
            return timeFormat.parse(timeString).time
        }
        fun formatTimestamp(timestamp: Long): String {
            val timeFormat = SimpleDateFormat("dd.MM.yyyy HH:mm")
            return timeFormat.format(Date(timestamp))
        }
        fun formatDateTime(day: Int, month: Int, year: Int, hour: Int, minute: Int): String {
            val formattedDay = String.format("%02d", day)
            val formattedMonth = String.format("%02d", month)
            val formattedYear = String.format("%04d", year)
            val formattedHour = String.format("%02d", hour)
            val formattedMinute = String.format("%02d", minute)

            return "$formattedDay.$formattedMonth.$formattedYear $formattedHour:$formattedMinute"
        }
        fun openDateAndTimePickerDialog(timeButton: AppCompatButton, timeLimit: Boolean, limit: Long?)
        fun showTimeErrorDialog()
    }

    // updates event list after sorting events by status
    fun updateEventList(updatedEvents: List<Event>) {
        events = updatedEvents
    }

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val eventName: TextView = itemView.findViewById(R.id.event_name)
        val eventDate: TextView = itemView.findViewById(R.id.event_date)
        val eventCard: CardView = itemView.findViewById(R.id.card)
        val eventSettings: ImageButton = itemView.findViewById(R.id.event_settings)
        val cardImage: ImageView = itemView.findViewById(R.id.card_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context).inflate(R.layout.card_event_item, parent, false)
        return EventViewHolder(adapterLayout)
    }

    override fun getItemCount(): Int {
        return events.size
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val currentEvent = events[position]

        // set event name
        holder.eventName.text = currentEvent.name

        // format date
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val formattedDate = currentEvent.eventDate?.let { dateFormat.format(it) }
        holder.eventDate.text = formattedDate

        // increase size of event card for active events
        if (status == "ACTIVE") {
            increaseCard(holder)
        }

        holder.eventCard.setOnClickListener {
            listener.onItemClicked(currentEvent)
        }

        holder.eventSettings.setOnClickListener {
            listener.onItemSettingsClicked(currentEvent, holder)
        }

        val eventViewModel = ViewModelProvider(holder.itemView.context as ViewModelStoreOwner)
            .get(eventViewModel::class.java)

        // set event picture as card image background
        if (currentEvent.reference != null) {
            GlobalScope.launch {
                val uri = eventViewModel.getUriFromPictureReference(currentEvent.reference!!)
                loadImageWithGlide(uri.toString(), holder.cardImage)
            }
        } else {
            val randomNumber = Random.nextInt(0, 3)
            when (randomNumber) {
                0 -> holder.cardImage.setImageResource(R.drawable.background1)
                1 -> holder.cardImage.setImageResource(R.drawable.background2)
                else -> holder.cardImage.setImageResource(R.drawable.background3)
            }
        }
    }

    // resize event card
    private fun increaseCard(holder: EventViewHolder) {
        val sizeCardEventActive = 624

        val layoutParams = holder.eventCard.layoutParams as ViewGroup.LayoutParams
        layoutParams.height = sizeCardEventActive
        layoutParams.width = sizeCardEventActive
    }

    // load image into card image view
    private fun loadImageWithGlide(imageUrl: String, imageView: ImageView) {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val bitmap = withContext(Dispatchers.IO) {
                    Glide.with(imageView.context)
                        .asBitmap()
                        .load(imageUrl)
                        .submit()
                        .get()
                }
                val drawable = BitmapDrawable(imageView.resources, bitmap)
                imageView.setImageDrawable(drawable)
            } catch (e: Exception) {
                Log.e("EventAdapter", "error loading image", e)
            }
        }
    }
}
