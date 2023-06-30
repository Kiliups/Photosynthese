package com.othregensburg.photosynthese

import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.othregensburg.photosynthese.databinding.ActivityProfileBinding
import com.othregensburg.photosynthese.databinding.FragmentEventDetailBinding
import com.othregensburg.photosynthese.models.Event
import com.othregensburg.photosynthese.models.eventViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class EventDetailFragment : Fragment() {

    private lateinit var descriptionCard: CardView
    private lateinit var timetableCard: CardView
    private lateinit var locationCard: CardView

    private lateinit var eVM: eventViewModel
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

        val image = view.findViewById<ImageView>(R.id.event_picture)

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
        else{
            image.visibility = View.GONE
        }

        //set the event description, timetable and location

        val description = view.findViewById<TextView>(R.id.description)
        val event_date = view.findViewById<TextView>(R.id.event_date)
        val posting_start = view.findViewById<TextView>(R.id.posting_start)
        val posting_end = view.findViewById<TextView>(R.id.posting_end)
        val location = view.findViewById<TextView>(R.id.location)
        val lineUnderDescription = view.findViewById<View>(R.id.line_under_description)
        val lineUnderTimetable = view.findViewById<View>(R.id.line_under_time_table)
        val description_title = view.findViewById<View>(R.id.description_title)
        val location_title = view.findViewById<View>(R.id.location_title)

        description.text = event?.description

        //if there is no description, hide the line under the description and the description titel
        if(event?.description == null || event?.description == ""){
            description_title.visibility = View.GONE
            description.visibility = View.GONE
            lineUnderDescription.visibility = View.GONE

        }

        //format date
        val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        var eventDate = event?.event_date?.let { dateFormat.format(it) }
        var postingStart = event?.start_date?.let { dateFormat.format(it) }
        var postingEnd = event?.end_date?.let { dateFormat.format(it) }

        event_date.text = eventDate
        posting_start.text = postingStart
        posting_end.text = postingEnd

        location.text = event?.location

        //if there is no location, hide the line under the timetable and the location titel
        if(event?.location == null || event?.location == ""){
            location_title.visibility = View.GONE
            lineUnderTimetable.visibility = View.GONE
            location.visibility = View.GONE
        }

        return view
    }

}

