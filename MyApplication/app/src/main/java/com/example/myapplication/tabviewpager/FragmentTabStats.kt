package com.example.myapplication.tabviewpager

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.models.DailyGoal
import com.example.myapplication.models.FocusStudyTime
import com.example.myapplication.models.Result
import com.example.myapplication.util.TimeCalculator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.layout_stats.view.*

// DATE class를 사용하기 위한 API제한
class FragmentTabStats : Fragment() {
    // database에 연결
    private lateinit var database: DatabaseReference
    // Log의 TAG
    companion object {
        private const val TAG = "FragmentTabStats"
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.layout_stats, container, false)
        // user의 uid를 확인하기 위해 우선 user가 존재하는지 확인
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Log.d(TAG, "user doesn't exist")
        } else {
            var uid = user.uid
            val database = Firebase.database
            val today = TimeCalculator().today()
            val myRef = database.getReference("calendar/$uid/$today")

            goalWrite(myRef)
            resultWrite(myRef)

            // [START read_message]
            // Read from the database
            myRef.child("/result").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val value = dataSnapshot.getValue<Result>()
                    val total = TimeCalculator().msToStringTime(value?.totalStudyTime!!)
                    view.textView.text = "총 공부시간 : $total"

                    Log.d(TAG,"real = ${value?.realStudyTime}")
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

    private fun goalWrite(myRef: DatabaseReference) {
        // [START write_message]
        // Write a message to the database
        val destination = myRef.child("/dailyGoal")
        val goalStatus: Boolean = false
        val timeString = TimeCalculator().currentTime()
        val goalTime: String = timeString
        val dailyGoal = DailyGoal(goalStatus, goalTime)
        destination.setValue(dailyGoal)
        // [END write_message]
    }
    private fun resultWrite(myRef: DatabaseReference){
        val destination = myRef.child("/result")
        val focusTime = FocusStudyTime("13:00:00","14:00:00")
        val focusStudyTime: List<FocusStudyTime> = listOf(focusTime)
        val maxFocusStudyTime = "00:30:00"
        val realStudyTime = "01:30:00"
        val totalStudyTime = 0L
        val result = Result(focusStudyTime,maxFocusStudyTime,realStudyTime,totalStudyTime)
        destination.setValue(result)
    }

}