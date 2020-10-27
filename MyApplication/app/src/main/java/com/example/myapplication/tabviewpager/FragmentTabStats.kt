package com.example.myapplication.tabviewpager

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.models.Dailygoal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.layout_stats.view.*
import java.sql.Time
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


class FragmentTabStats : Fragment() {
    // database에 연결
    private lateinit var database: DatabaseReference
    // Log의 TAG
    companion object {
        private const val TAG = "FragmentTabStats"
    }
    // DATE class를 사용하기 위한 API제한
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.layout_stats, container, false)
        // view의 textView의 text를 변경해줌
        view.textView.text = "된건가?"
        // user의 uid를 확인하기 위해 우선 user가 존재하는지 확인
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Log.d(TAG, "user doesn't exist")
        } else {
            var uid = user.uid
            val current = LocalDateTime.now(ZoneId.of("Asia/Seoul"))
            val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
            val formatted = current.format(dateFormatter)
            val database = Firebase.database
            val myRef = database.getReference("calendar/$uid/$formatted/dailygoal")
            basicReadWrite(myRef)
            // [START read_message]
            // Read from the database
            myRef.child("goaltime").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // This method is called once with the initial value and again
                    // whenever data at this location is updated.
                    val value = dataSnapshot.getValue<String>()
                    Log.d(TAG, "Value is: $value")
                    view.textView.text = "목표시간은 $value 입니다"
                }

                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read value.", error.toException())
                }
            })

        }
        // [END read_message]
        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun basicReadWrite(myRef: DatabaseReference) {
        // [START write_message]
        // Write a message to the database
        var goalstatus: Boolean = false
        val timeString = "10:30:00"
//        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
//        var goaltime: LocalTime = LocalTime.parse(timeString, timeFormatter)
        var goaltime = timeString
        val dailygoal = Dailygoal(goalstatus, goaltime)
        myRef.setValue(dailygoal)
        // [END write_message]


    }

}