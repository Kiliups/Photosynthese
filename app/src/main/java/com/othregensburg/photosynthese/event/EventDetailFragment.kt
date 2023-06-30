package com.othregensburg.photosynthese.event

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
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
    companion object {

        // create new instance of EventDetailFragment
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

        val eventViewModel = ViewModelProvider(this).get(eventViewModel::class.java)

        // get the event from the arguments
        val event = arguments?.getSerializable("event") as? Event

        // set the name of the given event
        val eventName = view.findViewById<TextView>(R.id.event_name)
        eventName.text = event?.name

        //set up the back button
        val backButton = view.findViewById<ImageButton>(R.id.back_button)
        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        val image = view.findViewById<ImageView>(R.id.event_picture)

        //set the event picture
        if(event?.reference != null) {
            lifecycleScope.launch {
                //get the uri from the event picture
                val uri = eventViewModel.getUriFromPictureReference(event.reference!!)
                //load the image into the imageview
                Glide.with(requireContext())
                    .load(uri)
                    .into(image)
            }
        } else{
            //if there is no picture, hide the imageview
            image.visibility = View.GONE
        }

        //set the event description, timetable and location
        val titleDescription = view.findViewById<View>(R.id.description_title)
        val description = view.findViewById<TextView>(R.id.description)
        val eventDate = view.findViewById<TextView>(R.id.event_date)
        val postingStart = view.findViewById<TextView>(R.id.posting_start)
        val postingEnd = view.findViewById<TextView>(R.id.posting_end)
        val titleLocation = view.findViewById<View>(R.id.location_title)
        val location = view.findViewById<TextView>(R.id.location)
        val lineUnderDescription = view.findViewById<View>(R.id.line_under_description)
        val lineUnderTimetable = view.findViewById<View>(R.id.line_under_time_table)

        description.text = event?.description

        //if there is no description, hide the line under the description and the description title
        if(event?.description == null || event.description == ""){
            titleDescription.visibility = View.GONE
            description.visibility = View.GONE
            lineUnderDescription.visibility = View.GONE
        }

        //format date
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val eventDateFormatted = event?.eventDate?.let { dateFormat.format(it) }
        val postingStartForamatted = event?.startDate?.let { dateFormat.format(it) }
        val postingEndFormatted = event?.endDate?.let { dateFormat.format(it) }

        eventDate.text = eventDateFormatted
        postingStart.text = postingStartForamatted
        postingEnd.text = postingEndFormatted

        location.text = event?.location

        //if there is no location, hide the line under the timetable and the location title
        if(event?.location == null || event.location == ""){
            titleLocation.visibility = View.GONE
            lineUnderTimetable.visibility = View.GONE
            location.visibility = View.GONE
        }

        return view
    }
}
