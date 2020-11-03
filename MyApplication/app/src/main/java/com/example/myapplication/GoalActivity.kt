package com.example.myapplication

import android.app.TimePickerDialog
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TimePicker
import com.example.myapplication.models.DailyGoal
import com.example.myapplication.tabviewpager.FragmentTabStats
import com.example.myapplication.util.TimeCalculator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_goal.*
import java.time.LocalTime
import java.util.*

class GoalActivity : AppCompatActivity() {
    // database에 연결
    private lateinit var database: DatabaseReference
    // Log의 TAG
    companion object {
        private const val TAG = "GoalActivity"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goal)

        val user = FirebaseAuth.getInstance().currentUser
        // Set a time change listener for time picker widget
        time_picker.setOnTimeChangedListener { view, hourOfDay, minute ->
            text_view.text = "Time(HH:MM) $hourOfDay" +
                    ": $minute"
        }

        // Set a click listener for set time button widget
        button_set.setOnClickListener {
            if (user == null) {
                Log.d(TAG, "user doesn't exist")
            } else {
                var uid = user.uid
                val database = Firebase.database
                val today = TimeCalculator().today()
                val myRef = database.getReference("calendar/$uid/$today")
                goalWrite(myRef,time_picker)
            }
        }

        // Set a click listener for get time button widget
        button_get.setOnClickListener {
            // Display the picker current time on text view
            text_view.text = "Time(HH:MM) ${getCurrentTime(time_picker)}"
        }
        time_picker.setIs24HourView(true)
    }

    // Custom method to get time picker current time as string
    private fun getCurrentTime(timePicker: TimePicker) {
        val current = LocalTime.now()
        timePicker.hour = current.hour
        timePicker.minute = current.minute

    }

    private fun goalWrite(myRef: DatabaseReference, timePicker: TimePicker) {
        // [START write_message]
        val destination = myRef.child("/dailyGoal/goalTime")
        var time = String.format("%02d:%02d:00",timePicker.hour,timePicker.minute)
        val goalTime = TimeCalculator().stringToLong(time)
        destination.setValue(goalTime)
        // [END write_message]
    }
}