package com.othregensburg.photosynthese

import android.R.attr.label
import android.R.attr.text
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.othregensburg.photosynthese.adapter.EventAdapter
import com.othregensburg.photosynthese.models.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var EventViewModel: eventViewModel

    // get current logged in user
    val user = FirebaseAuth.getInstance().currentUser

    var eventItemClickListener = object : EventAdapter.eventItemClickListener {
        override fun onItemClicked(event: Event) {
            val intent = Intent(this@MainActivity, EventActivity::class.java)
            intent.putExtra("event", event)
            startActivity(intent)
        }

        override fun onItemSettingsClicked(event: Event, holder: EventAdapter.EventViewHolder) {
            //When info button was clicked, dialog with option to copy event id is shown
            showEventPopupMenu(event, holder.eventSettings)

        }

        override fun showEventPopupMenu(event: Event, view: View) {
            val popup = PopupMenu(this@MainActivity, view)
            val menu: Menu = popup.menu
            popup.inflate(R.menu.event_settings)

            //check if logged in user is admin
            var isAdmin: Boolean = false
            for (admin in event.admins){
                if (admin == user!!.uid)
                    isAdmin = true
            }
            if(isAdmin){
                menu.findItem(R.id.leave_event).setVisible(false)
            } else {
                menu.findItem(R.id.delete_event).setVisible(false)
                menu.findItem(R.id.change_timetable).setEnabled(false)
            }

            popup.setOnMenuItemClickListener { item: MenuItem? ->

                when (item!!.itemId) {
                    R.id.copy_event_id -> {
                        showCopyIdDialog(event.id!!)
                    }

                    R.id.leave_event -> {
                        leaveEvent(event.id!!)
                    }
                    R.id.delete_event -> {
                        deleteEvent(event)
                    }
                    R.id.change_timetable -> {
                        showChangeTimeTableDialog(event)
                    }
                }

                true
            }

            popup.show()
        }
        override fun deleteEvent(event: Event){
            EventViewModel.delete(event)
            this@MainActivity.recreate()

        }

        override fun leaveEvent(event_id: String){
            EventViewModel.leaveEvent(user!!.uid, event_id)
            this@MainActivity.recreate()
        }

        override fun showChangeTimeTableDialog(event: Event) {

            // set up dialog
            val dialog = Dialog(this@MainActivity)
            dialog.setContentView(R.layout.dialog_change_timetable)
            dialog.window?.setBackgroundDrawable(getDrawable(R.drawable.background_dialog))
            dialog.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            dialog.setCancelable(false)

            // set texts to timestamp
            val eventTime = dialog.findViewById<AppCompatButton>(R.id.button_date_time)
            val startTime = dialog.findViewById<AppCompatButton>(R.id.button_startPostingDate_time)
            val endTime = dialog.findViewById<AppCompatButton>(R.id.button_endPostingDate_time)

            eventTime.text = formatTimestamp(event.event_date!!)
            startTime.text = formatTimestamp(event.start_date!!)
            endTime.text = formatTimestamp(event.end_date!!)

            // Handle click on buttons
            eventTime.setOnClickListener (object: View.OnClickListener{
                override fun onClick(p0: View?) {
                    openDateAndTimePickerDialog(eventTime)
                }
            })
            startTime.setOnClickListener (object: View.OnClickListener{
                override fun onClick(p0: View?) {
                    openDateAndTimePickerDialog(startTime)
                }
            })
            endTime.setOnClickListener (object: View.OnClickListener{
                override fun onClick(p0: View?) {
                    openDateAndTimePickerDialog(endTime)
                }
            })

            // Handle Apply Changes Button
            val applyChangesButton = dialog.findViewById<Button>(R.id.apply_changes_button)
            applyChangesButton.setOnClickListener(object: View.OnClickListener {
                override fun onClick(v: View?) {

                    event.event_date = parseTime(eventTime.text.toString())
                    event.start_date = parseTime(startTime.text.toString())
                    event.end_date = parseTime(endTime.text.toString())

                    EventViewModel.update(event)
                    dialog.dismiss()
                    this@MainActivity.recreate()
                }
            })

            // Button to close the showed dialog
            val dialogCloseButton = dialog.findViewById<ImageButton>(R.id.dialog_close_button)
            dialogCloseButton.setOnClickListener(object: View.OnClickListener {
                override fun onClick(p0: View?) {
                    dialog.dismiss()
                }
            })

            dialog.show()
        }

        override fun openDateAndTimePickerDialog(timeButton: AppCompatButton){
            // Create a Date object from the time string
            val date = Date(parseTime(timeButton.text.toString()))

            // Create a Calendar instance and set the date
            val calendar = Calendar.getInstance()
            calendar.time = date

            // Extract individual components
            var timestamp_day = calendar.get(Calendar.DAY_OF_MONTH)
            var timestamp_month = calendar.get(Calendar.MONTH) + 1
            var timestamp_year = calendar.get(Calendar.YEAR)
            var timestamp_hour = calendar.get(Calendar.HOUR_OF_DAY)
            var timestamp_minute = calendar.get(Calendar.MINUTE)

            val datePickerDialog = DatePickerDialog(this@MainActivity, R.style.dialog_theme, DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                timestamp_day = dayOfMonth
                timestamp_month = month+1
                timestamp_year = year

                //update and format button text
                timeButton.text = formatDateTime(timestamp_day, timestamp_month, timestamp_year, timestamp_hour, timestamp_minute)

                //Show timePicker, when date was selected
                val timePickerDialog = TimePickerDialog(this@MainActivity, R.style.dialog_theme, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                    timestamp_hour = hourOfDay
                    timestamp_minute = minute

                    //update and format button text
                    timeButton.text = formatDateTime(timestamp_day, timestamp_month, timestamp_year, timestamp_hour, timestamp_minute)

                },timestamp_hour,timestamp_minute, true )
                timePickerDialog.show()

            }, timestamp_year, timestamp_month-1, timestamp_day)

            datePickerDialog.datePicker.firstDayOfWeek = Calendar.MONDAY
            datePickerDialog.show()
        }

        override fun showCopyIdDialog(event_id: String){

            // set up dialog
            val dialog = Dialog(this@MainActivity)
            dialog.setContentView(R.layout.dialog_event_info)
            dialog.window?.setBackgroundDrawable(getDrawable(R.drawable.background_dialog))
            dialog.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            dialog.setCancelable(false)

            // set text to event id
            val event_id_TextView = dialog.findViewById<TextView>(R.id.event_id)
            event_id_TextView.text = event_id

            // Get button to copy the event id into the clipboard and set listener
            val dialogCopyCard = dialog.findViewById<CardView>(R.id.dialog_copy_card)
            dialogCopyCard.setOnClickListener(object: View.OnClickListener {
                override fun onClick(v: View?) {

                    //copy event code into clipboard
                    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip: ClipData = ClipData.newPlainText("event id", event_id)
                    clipboard.setPrimaryClip(clip)

                    //Show Toast if Android version below 13 (in API 33 other popup will inform user)
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2)
                        Toast.makeText(this@MainActivity, "event id copied into clipboard!", Toast.LENGTH_SHORT).show()
                }
            })

            // Button to close the showed dialog
            val dialogCloseButton = dialog.findViewById<Button>(R.id.dialog_close_button)
            dialogCloseButton.setOnClickListener(object: View.OnClickListener {
                override fun onClick(p0: View?) {
                    dialog.dismiss()
                }
            })

            dialog.show()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        //set up recycler views

        val activeEvents: RecyclerView = findViewById(R.id.recyclerView_events_active)
        activeEvents.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val activeAdapter = EventAdapter(emptyList(), "ACTIVE", eventItemClickListener)
        activeEvents.adapter = activeAdapter

        val futureEvents: RecyclerView = findViewById(R.id.recyclerView_events_future)
        futureEvents.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val futureAdapter = EventAdapter(emptyList(), "FUTURE", eventItemClickListener)
        futureEvents.adapter = futureAdapter

        val memoryEvents: RecyclerView = findViewById(R.id.recyclerView_events_memory)
        memoryEvents.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val memoryAdapter = EventAdapter(emptyList(), "MEMORY", eventItemClickListener)
        memoryEvents.adapter = memoryAdapter

        //set up event view model
        EventViewModel = ViewModelProvider(this).get(eventViewModel::class.java)
        var eventLiveData: LiveData<List<Event>> = EventViewModel.getEventsByUser(user!!.uid)

        eventLiveData.observe(this, androidx.lifecycle.Observer { events ->
            events?.let {

                //sort events by status
                var sortedEvents = EventViewModel.sortEventsByStatus(events)

                activeAdapter.updateEvents(sortedEvents[0])
                futureAdapter.updateEvents(sortedEvents[1])
                memoryAdapter.updateEvents(sortedEvents[2])
            }
        })

        // set up OnClickListener to create a new event
        val createEventButton: ImageButton = findViewById(R.id.icon_add)
        createEventButton.setOnClickListener(object: View.OnClickListener {
            override fun onClick (v: View?) {
                showPopupMenu(createEventButton)
            }
        })

        val profile=findViewById<ImageButton>(R.id.icon_profile)
        profile.setOnClickListener{
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        val swipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener {
            recreate()
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun showPopupMenu(view: View) {
        val popup = PopupMenu(this, view)
        popup.inflate(R.menu.add_event)

        popup.setOnMenuItemClickListener { item: MenuItem? ->

            when (item!!.itemId) {
                R.id.create_event -> {
                    val intent = Intent(this@MainActivity, EventCreateActivity::class.java)
                    startActivity(intent)
                }

                R.id.add_existing_event -> {
                    showAddEventDialog()
                }
            }

            true
        }

        popup.show()
    }

    private fun showAddEventDialog() {

        // set up dialog
        val dialog = Dialog(this@MainActivity)
        dialog.setContentView(R.layout.dialog_add_event)
        dialog.window?.setBackgroundDrawable(getDrawable(R.drawable.background_dialog))
        dialog.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setCancelable(false)

        // get editText for event_id
        val input_event_id = dialog.findViewById<EditText>(R.id.input_event_id)
        val add_event_button = dialog.findViewById<Button>(R.id.add_event_button)

        add_event_button.setOnClickListener(object: View.OnClickListener {
            override fun onClick(v: View?) {
                EventViewModel.addUserToEvent(user!!.uid, input_event_id.text.toString(), this@MainActivity)
                dialog.dismiss()
            }
        })

        // Button to close the showed dialog
        val dialogCloseButton = dialog.findViewById<ImageButton>(R.id.dialog_close_button)
        dialogCloseButton.setOnClickListener(object: View.OnClickListener {
            override fun onClick(p0: View?) {
                dialog.dismiss()
            }
        })

        dialog.show()
    }
}
