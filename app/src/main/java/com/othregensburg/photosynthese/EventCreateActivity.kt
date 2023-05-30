package com.othregensburg.photosynthese

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar

class EventCreateActivity : AppCompatActivity() {
    var timeButtonHour = 0
    var timeButtonMinute = 0
    var dateButtonDay = 0
    var dateButtonMonth = 0
    var dateButtonYear = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_create)

        //Button back to Main Activity
        val backButton = findViewById<ImageButton>(R.id.arrowBack)
        backButton.setOnClickListener(object: View.OnClickListener {
            override fun onClick (v: View?) {
                val intent = Intent(this@EventCreateActivity, MainActivity::class.java)
                startActivity(intent)
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