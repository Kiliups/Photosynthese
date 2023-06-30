package com.othregensburg.photosynthese.event

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import com.othregensburg.photosynthese.R
import com.othregensburg.photosynthese.databinding.FragmentEventCameraBinding
import com.othregensburg.photosynthese.models.Event
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class EventCameraFragment : Fragment() {
    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private lateinit var cameraExecutor: ExecutorService
    var cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    lateinit var camera: Camera

    var isWide = false
    var isWideAvailabile = false
    var flash = false
    var recordingVideo = false

    lateinit var event: Event

    lateinit var binding: FragmentEventCameraBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentEventCameraBinding.inflate(layoutInflater, container, false)

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
            )
        }

        event = (arguments?.getSerializable("event") as? Event)!!
        binding.title.text = event.name
        // Set up the listener for take photo button
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Set up video and photo toggle buttons
        binding.toggleGroup.check(binding.cameraToggle.id)
        binding.cameraToggle.setOnClickListener {
            if (recordingVideo) {
                binding.toggleGroup.check(binding.videoToggle.id)
            } else {
                binding.toggleGroup.check(binding.cameraToggle.id)
            }
        }
        binding.videoToggle.setOnClickListener {
            binding.toggleGroup.check(binding.videoToggle.id)
        }

        // Set up the listeners for take photo and video capture buttons
        binding.captureButton.setOnClickListener {
            // Animate the capture button
            var captureAnimation = AnimationUtils.loadAnimation(
                requireContext(), com.google.android.material.R.anim.abc_grow_fade_in_from_bottom
            )
            binding.captureButton.startAnimation(captureAnimation)
            // Capture photo or video
            if (binding.toggleGroup.checkedButtonId == binding.cameraToggle.id) {
                takePhoto()
            } else {
                captureVideo()
            }
        }

        // Set up the listener for flip camera button
        binding.flipCameraButton.setOnClickListener {
            // Flip the camera selector
            if (!recordingVideo) {
                when (cameraSelector) {
                    CameraSelector.DEFAULT_BACK_CAMERA -> {
                        cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                        startCamera()
                    }

                    CameraSelector.DEFAULT_FRONT_CAMERA -> {
                        cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                        startCamera()
                    }
                }
                setupFlashIcon()
            }
        }

        //Set up wide angle button
        binding.wideAngleButton.setOnClickListener {
            isWide = !isWide
            if (isWide) {
                binding.wideAngleButtonContainer.check(binding.wideAngleButton.id)
            } else {
                binding.wideAngleButtonContainer.uncheck(binding.wideAngleButton.id)
            }
            startCamera()
        }

        //Set up flash button
        setupFlashIcon()
        binding.flashButton.setOnClickListener {
            if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA && !recordingVideo) {
                flash = !flash
            }
            setupFlashIcon()
        }

        //Set up back button
        binding.backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        return binding.root
    }

    companion object {
        fun newInstance(event: Event): EventCameraFragment {
            val fragment = EventCameraFragment()
            val args = Bundle()
            args.putSerializable("event", event)
            fragment.arguments = args
            return fragment
        }

        // Implements CameraX
        private const val TAG = "Photosynthese"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = mutableListOf(
            Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO
        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()

    }

    fun setupFlashIcon() {
        if (flash) {
            if (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
                binding.flashButton.setImageResource(R.drawable.ic_flash_off_50)
            } else
                binding.flashButton.setImageResource(R.drawable.ic_flash_on_50)
        } else {
            binding.flashButton.setImageResource(R.drawable.ic_flash_off_50)
        }
    }

    fun setupFlash() {
        camera.cameraControl.enableTorch(flash)
        if (flash) {
            /*if (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
                binding.background.background = ColorDrawable(Color.WHITE)
            }*/
            if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                Thread.sleep(700)
            }
        }
    }

    //take photo and replace fragment
    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        //Set up flash
        setupFlash()

        // Create time-stamped output file to hold the image in the cache directory
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())
        val cacheDir = requireActivity().applicationContext.externalCacheDir
        val file = File(cacheDir, "$name.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

        // Set up image capture listener, which is triggered after photo has been taken
        imageCapture.takePicture(outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                //If error, log it
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                //save photo and replace fragment
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = output.savedUri ?: Uri.fromFile(file)
                    replaceFragment("photo", savedUri)
                    //Turn off flash
                    camera.cameraControl.enableTorch(false)
                }
            })
    }

    // Implements VideoCapture use case, including start and stop capturing.
    private fun captureVideo() {
        val videoCapture = this.videoCapture ?: return

        binding.captureButton.isEnabled = false

        val curRecording = recording
        if (curRecording != null) {
            // Stop the current recording session.
            curRecording.stop()
            recording = null
            return
        }

        // Create time-stamped output file to hold the video in the cache directory.
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())
        val cacheDir = requireActivity().applicationContext.externalCacheDir
        val file = File(cacheDir, "$name.mp4")
        val fileOutputOptions = FileOutputOptions.Builder(file).build()

        //Set up flash
        setupFlash()

        // Create a new video recording use case and start recording.
        recording =
            videoCapture.output.prepareRecording(requireContext(), fileOutputOptions).apply {
                if (PermissionChecker.checkSelfPermission(
                        requireContext(), Manifest.permission.RECORD_AUDIO
                    ) == PermissionChecker.PERMISSION_GRANTED
                ) {
                    withAudioEnabled()
                }
            }.start(ContextCompat.getMainExecutor(requireContext())) { recordEvent ->
                // Update the state of the button after the recording has started or stopped.
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {
                        recordingVideo = true
                        binding.captureButton.apply {
                            isEnabled = true
                            colorFilter = PorterDuffColorFilter(
                                ContextCompat.getColor(
                                    requireContext(), R.color.cards_pink
                                ), PorterDuff.Mode.SRC_ATOP
                            )
                            //hide wide angle button
                            binding.wideAngleButton.visibility = View.GONE
                        }
                    }

                    is VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()) {
                            // Video capture has stopped, output file is generated.
                            replaceFragment("video", recordEvent.outputResults.outputUri)
                        } else {
                            recording?.close()
                            recording = null
                            Log.e(
                                TAG, "Video capture ends with error: " + "${recordEvent.error}"
                            )
                        }
                        binding.captureButton.apply {
                            isEnabled = true
                        }
                        //turn off flash
                        camera.cameraControl.enableTorch(false)
                        recordingVideo = false
                    }
                }
            }
    }

    //replace fragment and send uri to fragment
    private fun replaceFragment(type: String, uri: Uri) {
        val fragment = EventCameraDisplayFragment.newInstance(event, uri, type)
        val fragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.addToBackStack("EventCameraDisplayFragment")
        fragmentTransaction.commit()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }

            //set recorder with quality and videoCapture
            val recorder = Recorder.Builder().setAspectRatio(AspectRatio.RATIO_4_3)
                .setQualitySelector(QualitySelector.from(Quality.HD)).build()
            videoCapture = VideoCapture.withOutput(recorder)

            // set imageCapture
            imageCapture = ImageCapture.Builder().build()

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, videoCapture
                )

                //check if wide angle camera is available
                wideAvalible()

                //if wide angle camera is available and user click on button, switch to wide angle camera
                if (isWide) {
                    val cameraControl = camera.cameraControl
                    val cameraInfo = camera.cameraInfo
                    val zoomRatio = cameraInfo.getZoomState().getValue()!!.getMinZoomRatio()
                    cameraControl.setZoomRatio(zoomRatio)
                }

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }


        }, ContextCompat.getMainExecutor(requireContext()))
    }

    //check if wide angle camera is available and setup button
    fun wideAvalible() {
        camera.cameraControl
        val cameraInfo = camera.cameraInfo
        val minZoomRatio = cameraInfo.getZoomState().getValue()!!.getMinZoomRatio()
        if (minZoomRatio < 1) {
            isWideAvailabile = true
            binding.wideAngleButton.visibility = View.VISIBLE
            binding.wideAngleButton.text = "x" + String.format("%.1f", minZoomRatio)
        } else {
            isWideAvailabile = false
            binding.wideAngleButton.visibility = View.GONE
        }
    }


    //check permission and start camera
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    //check permission and start camera
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(
                    requireContext(),
                    resources.getString(R.string.permissions_failed), Toast.LENGTH_SHORT
                ).show()
                requireActivity().finish()
            }
        }
    }
}