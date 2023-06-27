package com.othregensburg.photosynthese.adapter

import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.othregensburg.photosynthese.R
import com.othregensburg.photosynthese.models.Event
import com.othregensburg.photosynthese.models.eventViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
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
        val cardBackground: ImageView = itemView.findViewById(R.id.background_image)

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

        //set event title
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

        //val eVM = ViewModelProvider(this).get(eventViewModel::class.java)
        val eVM = ViewModelProvider(holder.itemView.context as ViewModelStoreOwner).get(eventViewModel::class.java)

        //set event picture as card background
        if(currentEvent.reference != null) {
            GlobalScope.launch {
                val uri = eVM.getUriFromPictureReference(currentEvent.reference!!)
                loadImageWithGlide(uri.toString(), holder.cardBackground)
            }
        }

    }

    //resize event card
    private fun resizeEventCard(holder: EventViewHolder,size: Int) {

        val layoutParams = holder.eventCard.layoutParams as ViewGroup.LayoutParams
        layoutParams.height = size
        layoutParams.width = size

        holder.eventCard.layoutParams = layoutParams
    }

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
                Log.e("EventAdapter", "Fehler beim Laden des Bildes", e)
            }
        }
    }

}