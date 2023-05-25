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

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
lateinit var binding: FragmentEventCameraDisplayBinding

/**
 * A simple [Fragment] subclass.
 * Use the [EventCameraDisplayFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EventCameraDisplayFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEventCameraDisplayBinding.inflate(inflater, container, false)
        val mediaViewModel= ViewModelProvider(this).get(mediaViewModel::class.java)
        val photo = arguments?.getParcelable<Uri>("photo")
        Glide.with(this).load(photo).into(binding.media)

        val video = arguments?.getParcelable<Uri>("video")
        if (video != null) {
            binding.video.setVideoURI(video)
            binding.video.start()
        }else
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

        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment EventCameraDisplayFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EventCameraDisplayFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}