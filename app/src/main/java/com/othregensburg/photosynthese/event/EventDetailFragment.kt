package com.othregensburg.photosynthese.event

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.othregensburg.photosynthese.R
import com.othregensburg.photosynthese.models.Event
import com.othregensburg.photosynthese.models.eventViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class EventDetailFragment : Fragment() {

    private lateinit var descriptionCard: CardView
    private lateinit var timetableCard: CardView
    private lateinit var locationCard: CardView

    private lateinit var eVM: eventViewModel

    companion object {

        // Create new instance of EventDetailFragment
        fun newInstance(event: Event): EventDetailFragment {
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
        eVM = ViewModelProvider(this).get(eventViewModel::class.java)

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

        val image = view.findViewById<ImageView>(R.id.imageView)

        //set the event picture
        if(event?.reference != null) {
            lifecycleScope.launch {
                //get the uri from the event picture
                val uri = eVM.getUriFromPictureReference(event?.reference!!)
                //load the image into the imageview
                Glide.with(requireContext())
                    .load(uri)
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

}

