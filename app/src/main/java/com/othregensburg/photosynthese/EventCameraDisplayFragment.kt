package com.othregensburg.photosynthese

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.othregensburg.photosynthese.databinding.FragmentEventCameraBinding
import com.othregensburg.photosynthese.databinding.FragmentEventCameraDisplayBinding
import com.othregensburg.photosynthese.models.Media
import com.othregensburg.photosynthese.models.mediaViewModel

lateinit var binding: FragmentEventCameraDisplayBinding
class EventCameraDisplayFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEventCameraDisplayBinding.inflate(inflater, container, false)
        val mediaViewModel = ViewModelProvider(this).get(mediaViewModel::class.java)
        val photo = arguments?.getParcelable<Uri>("photo")
        Glide.with(this).load(photo).into(binding.media)

        val video = arguments?.getParcelable<Uri>("video")
        if (video != null) {
            binding.video.setVideoURI(video)
            binding.video.start()
            binding.video.setOnPreparedListener { it.isLooping = true }
        } else
            binding.video.visibility = View.GONE

        binding.backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.sendButton.setOnClickListener {
            var media: Media? = null
            if (photo != null)
                media = Media(null, "0", null, System.currentTimeMillis(), null, photo)
            if (video != null)
                media = Media(null, "0", null, System.currentTimeMillis(), null, video)
            mediaViewModel.insert(media!!)
        }

        binding.sendButton.setBackgroundColor(resources.getColor(R.color.skyblue, null))

        return binding.root
    }

}