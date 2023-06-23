package com.othregensburg.photosynthese

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.BitmapFactory.decodeFile
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.setPadding
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.type.DateTime
import com.othregensburg.photosynthese.models.Event
import com.othregensburg.photosynthese.models.eventViewModel
import com.othregensburg.photosynthese.models.mediaViewModel
import java.security.Timestamp
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import kotlin.random.Random

class EventCreateActivity : AppCompatActivity() {
    var dateTimeButtonHour = 0
    var dateTimeButtonMinute = 0
    var dateTimeButtonDay = 0
    var dateTimeButtonMonth = 0
    var dateTimeButtonYear = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_create)

        val eventViewModel = ViewModelProvider(this).get(eventViewModel::class.java)

        val dateTimeButton = findViewById<Button>(R.id.button_date_time)

        //Button back to Main Activity
        val backButton = findViewById<ImageButton>(R.id.arrowBack)
        backButton.setOnClickListener(object: View.OnClickListener {
            override fun onClick (v: View?) {
                val intent = Intent(this@EventCreateActivity, MainActivity::class.java)
                startActivity(intent)
            }
        })

        val eventPictureButton = findViewById<ImageView>(R.id.eventPicture)

        var selectedPicture: Uri? = null;
        val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // Callback is invoked after the user selects a media item or closes the photo picker
            if (uri != null) {
                //If the user selected a picture
                eventPictureButton.setPadding(0)
                eventPictureButton.setImageURI(uri)
                selectedPicture = uri
            } else {
                //If no picture was selected
            }
        }
        eventPictureButton.setOnClickListener(object: View.OnClickListener{
            override fun onClick(p0: View?) {
                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        })

        val editText_eventName = findViewById<EditText>(R.id.editText_eventName)
        val editText_description = findViewById<EditText>(R.id.editText_description)
        val editText_location = findViewById<EditText>(R.id.editText_location)
        val sendButton = findViewById<Button>(R.id.CreateEventSendButton)
        val dialog = Dialog(this@EventCreateActivity)
        dialog.setContentView(R.layout.dialog_input_error)
        dialog.window?.setBackgroundDrawable(getDrawable(R.drawable.background_dialog))
        dialog.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setCancelable(false)
        val dialogOkButton = dialog.findViewById<Button>(R.id.dialog_ok_button)
        dialogOkButton.setOnClickListener(object: View.OnClickListener {
            override fun onClick(p0: View?) {
                dialog.dismiss()
            }
        })



        sendButton.setOnClickListener(object: View.OnClickListener {
            override fun onClick(p0: View?) {
                if (editText_eventName.text.toString().equals(""))
                    //No event name was put in
                    dialog.show()
                else {
                    //Create the event with selected inputs

                    //convert selected time to UNIX timestamp
                    val timeString = dateTimeButton.text.toString()
                    val timeFormat = SimpleDateFormat("dd.MM.yyyy HH:mm")
                    val timestamp = timeFormat.parse(timeString)

                    //create an Event object with the selected values
                    val newEvent = Event(
                        //FirebaseAuth.getInstance().currentUser.displayName
                        null,
                        editText_eventName.text.toString(),
                        timestamp.time,
                        timestamp.time,
                        timestamp.time + 43200000,
                        editText_location.text.toString(),
                        //listOf(FirebaseAuth.getInstance().currentUser.displayName),
                        listOf<String?>(null),
                        selectedPicture,
                        null,
                        null,
                        editText_description.text.toString()
                    )
                    eventViewModel.insert(newEvent)
                    val intent = Intent(this@EventCreateActivity, MainActivity::class.java)
                    startActivity(intent)

                }
            }
        })

        val currentDateTime = LocalDateTime.now(ZoneId.of("ECT"))
        dateTimeButton.text = currentDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
        dateTimeButtonDay = currentDateTime.dayOfMonth
        dateTimeButtonMonth = currentDateTime.monthValue
        dateTimeButtonYear = currentDateTime.year
        dateTimeButtonHour = currentDateTime.hour
        dateTimeButtonMinute = currentDateTime.minute


        dateTimeButton.setOnClickListener(object: View.OnClickListener {
            override fun onClick(v: View?) {
                openDateAndTimePickerDialog(dateTimeButton)
            }
        })

    }
    fun openDateAndTimePickerDialog(dateTimeButton: Button){
        val datePickerDialog = DatePickerDialog(this@EventCreateActivity, R.style.dialog_theme, DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            dateTimeButtonDay = dayOfMonth
            dateTimeButtonMonth = month+1
            dateTimeButtonYear = year

            updateTimeText(dateTimeButton)

            //Show timePicker, when date was selected
            val timePickerDialog = TimePickerDialog(this@EventCreateActivity, R.style.dialog_theme, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                dateTimeButtonHour = hourOfDay
                dateTimeButtonMinute = minute

                updateTimeText(dateTimeButton)
            },dateTimeButtonHour,dateTimeButtonMinute, true )
            timePickerDialog.show()

        }, dateTimeButtonYear, dateTimeButtonMonth-1, dateTimeButtonDay)
        datePickerDialog.datePicker.firstDayOfWeek = Calendar.MONDAY
        datePickerDialog.show()
    }

    fun updateTimeText(dateTimeButton: Button){
        var day = dateTimeButtonDay.toString()
        if (dateTimeButtonDay < 10)
            day = "0$day"
        var month = dateTimeButtonMonth.toString()
        if (dateTimeButtonMonth < 10)
            month = "0$month"

        var hour = dateTimeButtonHour.toString()
        if (dateTimeButtonHour<10)
            hour = "0$hour"
        var minute = dateTimeButtonMinute.toString()
        if(dateTimeButtonMinute<10)
            minute = "0$minute"

        dateTimeButton.text = "$day.$month.$dateTimeButtonYear $hour:$minute"
    }


}