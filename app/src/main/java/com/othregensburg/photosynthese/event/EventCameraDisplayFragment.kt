package com.othregensburg.photosynthese.event

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.othregensburg.photosynthese.R
import com.othregensburg.photosynthese.databinding.FragmentEventCameraDisplayBinding
import com.othregensburg.photosynthese.models.Event
import com.othregensburg.photosynthese.models.Media
import com.othregensburg.photosynthese.models.mediaViewModel


class EventCameraDisplayFragment : Fragment() {
    private lateinit var binding: FragmentEventCameraDisplayBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEventCameraDisplayBinding.inflate(inflater, container, false)
        val mediaViewModel = ViewModelProvider(this).get(mediaViewModel::class.java)

        // get photo from arguments and load it into view
        val photo = arguments?.getParcelable<Uri>("photo")
        Glide.with(this).load(photo).into(binding.photo)

        // get event from arguments
        val event = arguments?.getSerializable("event") as? Event

        // set title of event
        binding.title.text = event!!.name

        // get video from arguments and load it into view if it exists
        val video = arguments?.getParcelable<Uri>("video")
        if (video != null) {
            binding.video.setVideoURI(video)
            binding.video.start()
            binding.video.setOnPreparedListener { it.isLooping = true }
        } else
            binding.video.visibility = View.GONE

        // set back button
        binding.backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.progressBar.visibility = View.GONE
        binding.sendButton.setOnClickListener {

            // insert media into database
            var media: Media? = null
            if (photo != null)
                media = Media(
                    null, event.id, null, System.currentTimeMillis(), null, photo
                )
            if (video != null)
                media = Media(
                    null, event.id, null, System.currentTimeMillis(), null, video
                )
            mediaViewModel.insert(media!!)

            // display progress bar while loading
            mediaViewModel.isLoading.observe(viewLifecycleOwner, {
                if (it == true) {
                    binding.progressBar.visibility = View.VISIBLE
                } else {
                    binding.progressBar.visibility = View.GONE
                    requireActivity().onBackPressed()
                }
            })

            // disable button
            binding.sendButton.isEnabled = false
        }

        binding.sendButton.setBackgroundColor(resources.getColor(R.color.skyblue, null))

        return binding.root
    }

    companion object {
        fun newInstance(event: Event, uri: Uri, type: String): EventCameraDisplayFragment {
            val fragment = EventCameraDisplayFragment()
            val args = Bundle()
            args.putSerializable("event", event)
            args.putParcelable(type, uri)
            fragment.arguments = args
            return fragment
        }
    }

}