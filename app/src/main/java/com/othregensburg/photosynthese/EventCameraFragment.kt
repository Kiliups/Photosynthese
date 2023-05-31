package com.othregensburg.photosynthese

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.ColorDrawable
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.camera.camera2.Camera2Config
import androidx.camera.camera2.interop.Camera2CameraControl
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraFilter
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExtendableBuilder
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCapture.FLASH_MODE_OFF
import androidx.camera.core.ImageCapture.FLASH_MODE_ON
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
import com.othregensburg.photosynthese.databinding.FragmentEventCameraBinding
import java.io.File
import java.lang.Math.round
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


@ExperimentalCamera2Interop
class EventCameraFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var imageCapture: ImageCapture? = null

    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null

    private lateinit var cameraExecutor: ExecutorService
    var cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    lateinit var binding: FragmentEventCameraBinding
    var isWide = false
    var isWideAvailabile = false
    lateinit var camera: Camera
    var flash = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
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

        // Set up the listener for take photo button
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Set up the animation for the capture button
        var captureAnimation = AnimationUtils.loadAnimation(
            requireContext(), com.google.android.material.R.anim.abc_grow_fade_in_from_bottom
        )
        binding.toggleGroup.check(binding.cameraToggle.id)
        binding.cameraToggle.setOnClickListener {
            binding.toggleGroup.check(binding.cameraToggle.id)
        }
        binding.videoToggle.setOnClickListener {
            binding.toggleGroup.check(binding.videoToggle.id)
        }

        // Set up the listeners for take photo and video capture buttons
        binding.captureButton.setOnClickListener {
            binding.captureButton.startAnimation(captureAnimation)
            if (binding.toggleGroup.checkedButtonId == binding.cameraToggle.id) {
                takePhoto()
            } else {
                captureVideo()
            }
        }

        // Set up the listener for flip camera button
        binding.flipCameraButton.setOnClickListener {
            // Flip the camera selector
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
        }

        binding.wideAngleButton.setOnClickListener {
            isWide = !isWide
            if (isWide) {
                binding.wideAngleButtonContainer.check(binding.wideAngleButton.id)
            } else {
                binding.wideAngleButtonContainer.uncheck(binding.wideAngleButton.id)
            }
            startCamera()
        }
        binding.backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        if (flash) {
            binding.flashButton.setImageResource(R.drawable.ic_flash_on_50)
        } else {
            binding.flashButton.setImageResource(R.drawable.ic_flash_off_50)
        }
        binding.flashButton.setOnClickListener {
            flash = !flash
            if (flash) {
                binding.flashButton.setImageResource(R.drawable.ic_flash_on_50)
            } else {
                binding.flashButton.setImageResource(R.drawable.ic_flash_off_50)
            }
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
         * @return A new instance of fragment CameraFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) = EventCameraFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_PARAM1, param1)
                putString(ARG_PARAM2, param2)
            }
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

    //take photo and replace fragment
    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        camera.cameraControl.enableTorch(flash)
        if (flash) {
            if (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA)
                binding.background.background = ColorDrawable(Color.WHITE)
            Thread.sleep(500)
        }

        // Create time-stamped output file to hold the image in the cache directory
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())
        val cacheDir = requireActivity().applicationContext.externalCacheDir
        val file = File(cacheDir, "$name.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

        // Set up image capture listener, which is triggered after photo has been taken
        imageCapture.takePicture(outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                //save photo and replace fragment
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = output.savedUri ?: Uri.fromFile(file)
                    val msg = "Photo capture succeeded"
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                    replaceFragment("photo", savedUri)
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

        camera.cameraControl.enableTorch(flash)
        if (flash) {
            if (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
                binding.background.background = ColorDrawable(Color.WHITE)
            }
            Thread.sleep(500)
        }

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
                        binding.captureButton.apply {
                            isEnabled = true
                            colorFilter = PorterDuffColorFilter(
                                ContextCompat.getColor(
                                    requireContext(), R.color.cards_pink
                                ), PorterDuff.Mode.SRC_ATOP
                            )

                        }
                    }

                    is VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()) {
                            // Video capture has stopped, output file is generated.
                            val msg =
                                "Video capture succeeded: " + "${recordEvent.outputResults.outputUri}"
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                            Log.d(TAG, msg)
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
                        camera.cameraControl.enableTorch(false)
                    }
                }
            }
    }

    //replace fragment and send uri to fragment
    private fun replaceFragment(type: String, uri: Uri) {
        val extras = Bundle()
        extras.putParcelable(
            type, uri
        )
        val fragment = EventCameraDisplayFragment()
        fragment.arguments = extras
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

                wideAvalible()

                if (isWide) {
                    val cameraControl = camera.cameraControl
                    val cameraInfo = camera.cameraInfo
                    val zoomRatio = cameraInfo.getZoomState().getValue()!!.getMinZoomRatio()
                    cameraControl.setZoomRatio(zoomRatio)
                    Log.d("Camera2wide", "zoomRatio: $zoomRatio")
                }

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }


        }, ContextCompat.getMainExecutor(requireContext()))
    }

    fun wideAvalible() {
        val cameraControl = camera.cameraControl
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
                    requireContext(), "Permissions not granted by the user.", Toast.LENGTH_SHORT
                ).show()
                requireActivity().finish()
            }
        }
    }

}

