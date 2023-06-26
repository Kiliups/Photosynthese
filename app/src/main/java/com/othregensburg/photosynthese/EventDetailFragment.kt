package com.othregensburg.photosynthese

import android.net.Uri
import android.os.Bundle
import android.text.format.DateUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.othregensburg.photosynthese.models.Event
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class EventDetailFragment : Fragment() {

    private lateinit var descriptionCard: CardView
    private lateinit var timetableCard: CardView
    private lateinit var locationCard: CardView

    companion object {

        // Create new instance of EventDetailFragment
        fun newInstance(event: Event): EventDetailFragment{
            val fragment = EventDetailFragment()
            val args = Bundle()
            args.putSerializable("event", event)
            fragment.arguments = args
            return fragment
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_event_detail, container, false)

        // Get the event from the arguments
        val event = arguments?.getSerializable("event") as? Event

        // Set the event name
        var eventname = view.findViewById<TextView>(R.id.event_name)
        eventname.text = event?.name

        //set up the back button
        val backButton = view.findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        //set the event picture
        val image = view.findViewById<ImageView>(R.id.imageView)
        event?.picture?.let {
            // Use a coroutine to fetch the URI asynchronously
            lifecycleScope.launch {
                Glide.with(requireContext())
                    .load(it)
                    .into(image)
            }
        }

        //set the event description, timetable and location

        descriptionCard = view.findViewById(R.id.expandable_card_description)
        timetableCard = view.findViewById(R.id.expandable_card_timetable)
        locationCard = view.findViewById(R.id.expandable_card_location)

        descriptionCard.findViewById<TextView>(R.id.info_title).setText(R.string.description)
        timetableCard.findViewById<TextView>(R.id.info_title).setText(R.string.time_table)
        locationCard.findViewById<TextView>(R.id.info_title).setText(R.string.location)

        descriptionCard.findViewById<TextView>(R.id.details).setText("\n" + event?.description)

        //format date
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        var eventDate = event?.event_date?.let { dateFormat.format(it) }
        var start = event?.start_date?.let { dateFormat.format(it) }
        var end = event?.end_date?.let { dateFormat.format(it) }

        val eventDateString = getString(R.string.event_date)
        val postingStartString = getString(R.string.posting_start)
        val postingEndString = getString(R.string.posting_end)
        timetableCard.findViewById<TextView>(R.id.details).setText("\n" + eventDateString + ": "+ eventDate + "\n\n" + postingStartString + ": " + start + "\n" + postingEndString + ": " + end)

        locationCard.findViewById<TextView>(R.id.details).setText("\n" + event?.location)

        descriptionCard.setOnClickListener { expand(descriptionCard) }
        timetableCard.setOnClickListener { expand(timetableCard) }
        locationCard.setOnClickListener { expand(locationCard) }

        return view
    }


    // expands the card
    private fun expand(card: CardView) {

        var details = card.findViewById<TextView>(R.id.details)
        var icon = card.findViewById<ImageView>(R.id.expand_icon)

        // If the card is not expanded, expand it
        if(details.visibility == View.GONE) {
            details.visibility = View.VISIBLE
            icon.setImageResource(R.drawable.ic_arrow_drop_up_24)
        } else {
            details.visibility = View.GONE
            icon.setImageResource(R.drawable.ic_arrow_drop_down_24)
        }

    }

    //get the uri from the event picture
    private suspend fun getUriFromPicture(picture: String): Uri {

        val storageRef: StorageReference = FirebaseStorage.getInstance().getReference()

        var uri: Uri = Uri.EMPTY

        //download event picture uri from firebase storage
        if(picture!=null){
            uri = storageRef.child(picture!!).downloadUrl.await()
            //event.content = uri
        }

        return uri

    }

}

