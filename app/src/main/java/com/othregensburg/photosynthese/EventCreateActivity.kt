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
    var timeButtonHour = 0
    var timeButtonMinute = 0
    var dateButtonDay = 0
    var dateButtonMonth = 0
    var dateButtonYear = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_create)

        val eventViewModel = ViewModelProvider(this).get(eventViewModel::class.java)

        //Button back to Main Activity
        val backButton = findViewById<ImageButton>(R.id.arrowBack)
        backButton.setOnClickListener(object: View.OnClickListener {
            override fun onClick (v: View?) {
                val intent = Intent(this@EventCreateActivity, MainActivity::class.java)
                startActivity(intent)
            }
        })

        val eventPictureButton = findViewById<ImageView>(R.id.eventPicture)
        val randomNumber = Random.nextInt(6)
        when (randomNumber) {
            1 -> eventPictureButton.background = getDrawable(R.color.cards_pink)
            2 -> eventPictureButton.background = getDrawable(R.color.cards_yellow)
            3 -> eventPictureButton.background = getDrawable(R.color.cards_sage)
            4 -> eventPictureButton.background = getDrawable(R.color.cards_lime)
            5 -> eventPictureButton.background = getDrawable(R.color.cards_teal)
            else -> eventPictureButton.background = getDrawable(R.color.cards_purple)
        }
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
                    val timeString = "${dateButtonDay}.${dateButtonMonth}.${dateButtonYear} ${timeButtonHour}:${timeButtonMinute}:"
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

        val dateButton = findViewById<Button>(R.id.button_date)
        val timeButton = findViewById<Button>(R.id.button_time)
        val currentDateTime = LocalDateTime.now(ZoneId.of("ECT"))

        dateButton.text = currentDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        dateButtonDay = currentDateTime.dayOfMonth
        dateButtonMonth = currentDateTime.monthValue
        dateButtonYear = currentDateTime.year
        timeButton.text = currentDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        timeButtonHour = currentDateTime.hour
        timeButtonMinute = currentDateTime.minute


        dateButton.setOnClickListener(object: View.OnClickListener {
            override fun onClick(v: View?) {
                openDateAndTimePickerDialog(dateButton, timeButton)
            }
        })
        timeButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                openTimePickerDialog(timeButton)
            }
        })



    }
    fun openDateAndTimePickerDialog(dateButton: Button, timeButton: Button){
        val datePickerDialog = DatePickerDialog(this@EventCreateActivity, R.style.dialog_theme, DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->

            dateButton.text = "$dayOfMonth."+ (month+1)+".$year"
            dateButtonDay = dayOfMonth
            dateButtonMonth = month+1
            dateButtonYear = year

            //Show timePicker, when date was selected
            openTimePickerDialog(timeButton)

        }, dateButtonYear, dateButtonMonth-1, dateButtonDay)
        datePickerDialog.datePicker.firstDayOfWeek = Calendar.MONDAY
        datePickerDialog.show()
    }

    fun openTimePickerDialog(timeButton: Button){
        val timePickerDialog = TimePickerDialog(this@EventCreateActivity, R.style.dialog_theme, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
            if (minute > 9 && hourOfDay>9)
                timeButton.text = "$hourOfDay:$minute"
            else if (minute > 9)
                timeButton.text = "0$hourOfDay:$minute"
            else if (hourOfDay > 9)
                timeButton.text = "$hourOfDay:0$minute"
            else
                timeButton.text = "0$hourOfDay:0$minute"
            timeButtonHour = hourOfDay
            timeButtonMinute = minute
        },timeButtonHour,timeButtonMinute, true )
        timePickerDialog.show()
    }

}