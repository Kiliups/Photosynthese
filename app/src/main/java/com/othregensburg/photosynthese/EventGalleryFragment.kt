package com.othregensburg.photosynthese

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.othregensburg.photosynthese.databinding.FragmentEventGalleryBinding
import com.othregensburg.photosynthese.models.Event
import com.othregensburg.photosynthese.models.Media
import com.othregensburg.photosynthese.models.mediaViewModel
import com.othregensburg.photosynthese.models.userViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class EventGalleryFragment : Fragment() {
    lateinit var binding: FragmentEventGalleryBinding
    var counter = 0
    var event: Event? = null
    val auth = FirebaseAuth.getInstance()
    var isAdmin = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentEventGalleryBinding.inflate(inflater, container, false)
        val mVM = ViewModelProvider(requireActivity()).get(mediaViewModel::class.java)
        event = arguments?.getSerializable("event") as? Event
        binding.title.text = event!!.name
        mVM.getEventMedia(event!!.id).observe(viewLifecycleOwner) { mediaList ->
            if (mediaList != null && mediaList.isNotEmpty()) {
                binding.noResults.visibility = View.GONE
                //load media from database and display progress bar while loading
                mVM.isLoading.observe(viewLifecycleOwner) {
                    if (it == true) {
                        binding.progressBar.visibility = View.VISIBLE
                    } else {
                        binding.progressBar.visibility = View.GONE
                    }
                }

                //display media in view
                var media = mediaList[counter]
                displaymedia(media)

                //display next media
                binding.next.setOnClickListener {
                    if (counter < mediaList.size - 1) {
                        counter++
                        media = mediaList.get(counter)
                        displaymedia(media)
                    }
                }

                //display previous media
                binding.previous.setOnClickListener {
                    if (counter > 0) {
                        counter--
                        media = mediaList.get(counter)
                        displaymedia(media)
                    }
                }

                //set up more button
                binding.moreButton.setOnClickListener { view ->
                    moreButton(view, mediaList, media, mVM)
                }
            } else {
                binding.progressBar.visibility = View.GONE
                binding.userContainer.visibility = View.GONE
                binding.mediaContainer.visibility = View.GONE
            }
        }


        //set up back button
        binding.backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }
        return binding.root
    }

    private fun displaymedia(media: Media) {
        //define media type
        var type = media.reference.toString()
        type = type.substring(type.lastIndexOf(".") + 1)

        if (type == "jpg") {
            //display photo if media is a photo
            binding.video.visibility = View.GONE
            binding.photo.visibility = View.VISIBLE
            Glide.with(this).load(media.content).into(binding.photo)
        } else {
            //display video if media is a video
            binding.photo.visibility = View.GONE
            binding.video.visibility = View.VISIBLE
            binding.video.setVideoURI(media.content)
            binding.video.start()
            binding.video.setOnPreparedListener { it.isLooping = true }
        }

        //display timestamp as time
        val dateFormat = SimpleDateFormat("dd.MM.yyyy, HH:mm:ss", Locale.getDefault())
        val time = Date(media.timestamp!!)
        binding.time.text = dateFormat.format(time)

        //display username and profile picture
        if (media.user != null) {
            val uVM = ViewModelProvider(requireActivity()).get(userViewModel::class.java)
            uVM.getUser(media.user!!).observe(viewLifecycleOwner) { user ->
                if (user != null) {
                    binding.username.text = user.username
                    if (user.picture != null) {
                        binding.pictureContainer.visibility = View.VISIBLE
                        Glide.with(requireContext()).load(user.picture).into(binding.profilePicture)
                    }else
                        binding.pictureContainer.visibility= View.GONE
                }
            }
        } else {
            binding.username.text = "Username"
        }

        //set isAdmin true if user is admin or owner of media
        isAdmin = auth.currentUser!!.uid == media.user || event!!.admins.contains(auth.currentUser!!.uid)
    }

    fun moreButton(view: View, mediaList: MutableList<Media>, _media: Media, mVM: mediaViewModel) {
        var media = _media
        //set up popup menu
        val postMenu = PopupMenu(requireContext(), view)
        postMenu.inflate(R.menu.post_menu)
        //show delete button only if user is admin or owner of media
        postMenu.menu.getItem(1).isVisible = isAdmin
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
                    } else {
                        if (counter < mediaList.size - 1) {
                            media = mediaList.get(counter)
                            displaymedia(media)
                        } else {
                            requireActivity().onBackPressed()
                        }
                    }
                    true
                }

                R.id.download -> {
                    if (media.content != null) {
                        //save media to device in gallery folder
                        mVM.saveMedia(media)
                        mVM.isDone.observe(viewLifecycleOwner) {
                            if (it == true) {
                                Toast.makeText(
                                    requireContext(),
                                    resources.getString(R.string.media_saved),
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    resources.getString(R.string.media_saved_error),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                    true
                }

                else -> false
            }
        }
        try {
            val fieldMPopup = PopupMenu::class.java.getDeclaredField("mPopup")
            fieldMPopup.isAccessible = true
            val mPopup = fieldMPopup.get(postMenu)
            mPopup.javaClass.getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                .invoke(mPopup, true)
        } catch (_: Exception) {

        }
        postMenu.show()
    }

    companion object {
        fun newInstance(event: Event): EventGalleryFragment {
            val fragment = EventGalleryFragment()
            val args = Bundle()
            args.putSerializable("event", event)
            fragment.arguments = args
            return fragment
        }
    }

}