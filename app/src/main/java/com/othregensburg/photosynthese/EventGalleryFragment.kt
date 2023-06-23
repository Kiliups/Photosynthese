package com.othregensburg.photosynthese

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.PopupMenu
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.othregensburg.photosynthese.databinding.FragmentEventGalleryBinding
import com.othregensburg.photosynthese.models.Media
import com.othregensburg.photosynthese.models.mediaViewModel
import com.othregensburg.photosynthese.models.userViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EventGalleryFragment : Fragment() {
    lateinit var binding: FragmentEventGalleryBinding
    var counter = 0
    var size: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentEventGalleryBinding.inflate(inflater, container, false)
        val mVM = ViewModelProvider(requireActivity()).get(mediaViewModel::class.java)
        mVM.getEventMedia().observe(viewLifecycleOwner) { mediaList ->
            if (mediaList != null && mediaList.isNotEmpty()) {
                mVM.isLoading.observe(viewLifecycleOwner) {
                    if (it == true) {
                        binding.progressBar.visibility = View.VISIBLE
                    } else {
                        binding.progressBar.visibility = View.GONE
                    }
                }
                var media = mediaList[counter]
                displaymedia(media)
                binding.next.setOnClickListener {
                    if (counter < mediaList.size - 1) {
                        counter++
                        media = mediaList.get(counter)
                        displaymedia(media)
                    }
                }
                binding.previous.setOnClickListener {
                    if (counter > 0) {
                        counter--
                        media = mediaList.get(counter)
                        displaymedia(media)
                    }
                }
                binding.moreButton.setOnClickListener { view ->
                    val postMenu = PopupMenu(requireContext(), view)
                    postMenu.inflate(R.menu.post_menu)
                    postMenu.setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.delete -> {
                                //delete media from database
                                mVM.delete(media)
                                mediaList.remove(media)
                                if (counter > 0) {
                                    counter--
                                    media = mediaList.get(counter)
                                    displaymedia(media)
                                }else{
                                    if (counter < mediaList.size - 1) {
                                        media = mediaList.get(counter)
                                        displaymedia(media)
                                    }else{
                                        requireActivity().onBackPressed()
                                    }
                                }
                                true
                            }

                            R.id.download -> {
                                if (media.content != null) {
                                    //save media to device in gallery folder
                                    mVM.saveMedia(media!!)
                                }
                                true
                            }

                            else -> false
                        }
                    }
                    postMenu.show()
                }
            }
        }
        return binding.root
    }

    private fun displaymedia(media: Media) {
        var type = media.reference.toString()
        type = type.substring(type.lastIndexOf(".") + 1)
        if (type == "jpg") {
            binding.video.visibility = View.GONE
            binding.photo.visibility = View.VISIBLE
            Glide.with(this).load(media.content).into(binding.photo)
        } else {
            binding.photo.visibility = View.GONE
            binding.video.visibility = View.VISIBLE
            binding.video.setVideoURI(media.content)
            binding.video.start()
            binding.video.setOnPreparedListener { it.isLooping = true }
        }
        val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val time = Date(media.timestamp!!)
        binding.time.text = dateFormat.format(time)
        if (media.user != null) {
            val uVM = ViewModelProvider(requireActivity()).get(userViewModel::class.java)
            uVM.getUser(media.user!!).observe(viewLifecycleOwner) { user ->
                if (user != null) {
                    binding.username.text = user.username
                }
            }
        } else {
            binding.username.text = "Username"
        }
    }

}