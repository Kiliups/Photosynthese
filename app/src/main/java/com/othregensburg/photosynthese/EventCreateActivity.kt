package com.othregensburg.photosynthese

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.setPadding
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.othregensburg.photosynthese.models.Event
import com.othregensburg.photosynthese.models.eventViewModel
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar

class EventCreateActivity : AppCompatActivity() {
    var dateTimeButtonHour = 0
    var dateTimeButtonMinute = 0
    var dateTimeButtonDay = 0
    var dateTimeButtonMonth = 0
    var dateTimeButtonYear = 0
    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_create)

        val eventViewModel = ViewModelProvider(this).get(eventViewModel::class.java)

        val dateTimeButton = findViewById<Button>(R.id.button_date_time)

        // button back to Main Activity
        val backButton = findViewById<ImageButton>(R.id.arrowBack)
        backButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val intent = Intent(this@EventCreateActivity, MainActivity::class.java)
                startActivity(intent)
            }
        })

        // button to choose and show event picture
        val eventPictureButton = findViewById<ImageView>(R.id.eventPicture)

        var selectedPicture: Uri? = null
        val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // Callback is invoked after the user selects a media item or closes the photo picker
            if (uri != null) {
                //If the user selected a picture
                eventPictureButton.setPadding(0)
                eventPictureButton.setImageURI(uri)
                selectedPicture = uri
            }
        }
        // listener to choose event picture
        eventPictureButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        })

        // Input fields of form
        val editTextEventName = findViewById<EditText>(R.id.editText_eventName)
        val editTextDescription = findViewById<EditText>(R.id.editText_description)
        val editTextLocation = findViewById<EditText>(R.id.editText_location)
        val sendButton = findViewById<Button>(R.id.CreateEventSendButton)

        // Show dialog if input was wrong (no event name set)
        val dialog = Dialog(this@EventCreateActivity)
        dialog.setContentView(R.layout.dialog_input_error)
        dialog.window?.setBackgroundDrawable(getDrawable(R.drawable.background_dialog))
        dialog.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setCancelable(false)

        val dialogOkButton = dialog.findViewById<Button>(R.id.dialog_ok_button)
        dialogOkButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                dialog.dismiss()
            }
        })

        // button to create event was clicked
        sendButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                if (editTextEventName.text.toString().equals("")) {
                    // No event name was put in
                    dialog.show()
                } else {
                    // Create the event with selected inputs
                    val user = FirebaseAuth.getInstance().currentUser

                    // convert selected time to UNIX timestamp
                    val timeString = dateTimeButton.text.toString()
                    val timeFormat = SimpleDateFormat("dd.MM.yyyy HH:mm")
                    val timestamp = timeFormat.parse(timeString)

                    // create an Event object with the selected values
                    val newEvent = Event(
                        mutableListOf<String?>(user!!.uid),
                        editTextEventName.text.toString(),
                        timestamp.time,
                        timestamp.time,
                        timestamp.time + 43200000,
                        editTextLocation.text.toString(),
                        mutableListOf<String?>(user!!.uid),
                        selectedPicture,
                        null,
                        null,
                        editTextDescription.text.toString(),
                        null
                    )

                    // insert event into Firestore
                    eventViewModel.insert(newEvent)
                    val intent = Intent(this@EventCreateActivity, MainActivity::class.java)
                    startActivity(intent)
                }
            }
        })

        // get current date as initial time for the event
        val currentDateTime = LocalDateTime.now(ZoneId.of("ECT"))
        dateTimeButton.text = currentDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
        dateTimeButtonDay = currentDateTime.dayOfMonth
        dateTimeButtonMonth = currentDateTime.monthValue
        dateTimeButtonYear = currentDateTime.year
        dateTimeButtonHour = currentDateTime.hour
        dateTimeButtonMinute = currentDateTime.minute

        dateTimeButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                openDateAndTimePickerDialog(dateTimeButton)
            }
        })
    }
    fun openDateAndTimePickerDialog(dateTimeButton: Button) {
        // first show the date picker with the current set date
        val datePickerDialog = DatePickerDialog(
            this@EventCreateActivity,
            R.style.dialog_theme,
            DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                dateTimeButtonDay = dayOfMonth
                dateTimeButtonMonth = month+1
                dateTimeButtonYear = year

                updateTimeText(dateTimeButton)

                // show timePicker, when date was selected
                val timePickerDialog = TimePickerDialog(
                    this@EventCreateActivity,
                    R.style.dialog_theme,
                    TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                        dateTimeButtonHour = hourOfDay
                        dateTimeButtonMinute = minute

                        updateTimeText(dateTimeButton)
                    },
                    dateTimeButtonHour,
                    dateTimeButtonMinute,
                    true
                )
                timePickerDialog.show()
            },
            dateTimeButtonYear,
            dateTimeButtonMonth - 1,
            dateTimeButtonDay
        )
        datePickerDialog.datePicker.firstDayOfWeek = Calendar.MONDAY
        datePickerDialog.show()
    }

    fun updateTimeText(dateTimeButton: Button) {
        val formattedDay = String.format("%02d", dateTimeButtonDay)
        val formattedMonth = String.format("%02d", dateTimeButtonMonth)
        val formattedHour = String.format("%02d", dateTimeButtonHour)
        val formattedMinute = String.format("%02d", dateTimeButtonMinute)

        dateTimeButton.text = "$formattedDay.$formattedMonth.$dateTimeButtonYear $formattedHour:$formattedMinute"
    }
}
