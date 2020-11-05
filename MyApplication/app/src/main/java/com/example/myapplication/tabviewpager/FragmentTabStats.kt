package com.example.myapplication.tabviewpager

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapplication.GoalActivity
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
import kotlinx.android.synthetic.main.layout_stats.*
import kotlinx.android.synthetic.main.layout_stats.view.*
import java.text.SimpleDateFormat
import java.util.*


// DATE class를 사용하기 위한 API제한
class FragmentTabStats : Fragment() {
    // database에 연결
    private lateinit var database: DatabaseReference
    // Log의 TAG
    companion object {
        private const val TAG = "FragmentTabStats"
    }
    var calendar: Calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH) +1
    val date = calendar.get(Calendar.DATE)

    var myDatePicker =
        OnDateSetListener { view, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateLabel()
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

            resultWrite(myRef)

            // [START read_message]
            myRef.child("/result").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val value = dataSnapshot.getValue<Result>()
                    if(value == null){
                        Log.d(TAG,"value가 없음")
                    }else {
                        val total = TimeCalculator().msToStringTime(value.totalStudyTime)
                        // 총 공부 시간
                        view.timeText.text = "$total"
                        // 실제 공부한 시간
                        val real = TimeCalculator().msToStringTime(value.realStudyTime)
                        view.realTime.text = "$real"
                        // 공부에 집중한 시간
                        var list = value.focusStudyTime
                        var string:String = ""
                        list = list.sortedWith(Comparator { data1, data2 ->
                            (TimeCalculator().stringToLong(data2.endTime)-TimeCalculator().stringToLong(data2.startTime))
                                .compareTo((TimeCalculator().stringToLong(data1.endTime))-TimeCalculator().stringToLong(data1.startTime))
                        })

                        for (i in list.indices){
                            if(i == 3)break
                            string += "${list[i].startTime} ~ ${list[i].endTime}\n"
                        }
                        view.recommendTime.text = "${string}"
                        //최대 공부 시간 : maxFocusStudyTime
                        val max = TimeCalculator().msToStringTime(value.maxFocusStudyTime)
                        view.maxFocusTime.text = "${max}"

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read value.", error.toException())
                }
            })

            myRef.child("/dailyGoal").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val value = dataSnapshot.getValue<DailyGoal>()
                    if (value == null) {
                        Log.d(TAG, "value가 없음")
                    } else {
                        val goal = TimeCalculator().msToStringTime(value.goalTime)
                        view.goalTime.text = "$goal"
                        Log.d(TAG, "$goal")
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read value.", error.toException())
                }
            })
            // [END read_message]
            view.calendarText.text = "$year.$month.$date"

            val tv_date = view.calendarText
            tv_date.setOnClickListener {
                context?.let { it1 ->
                    DatePickerDialog(
                        it1, R.style.DatePickerTheme,
                        myDatePicker,
                        calendar[Calendar.YEAR],
                        calendar[Calendar.MONTH],
                        calendar[Calendar.DAY_OF_MONTH]
                    ).show()
                }
            }
        }
        view.table_layout.setOnLongClickListener {
            startActivity(Intent(this.activity,GoalActivity::class.java))
            return@setOnLongClickListener true
        }

        return view
    }

    private fun resultWrite(myRef: DatabaseReference){
        val destination = myRef.child("/result")
        val focusTime = FocusStudyTime("13:00:00","14:00:00")
        val focusStudyTime1 = FocusStudyTime("10:00:00","12:00:00")
        val focusStudyTime3 = FocusStudyTime("00:00:00","04:00:00")
        val focusStudyTime4 = FocusStudyTime("04:00:01","04:01:00")
        val focusStudyTime5 = FocusStudyTime("04:05:00","04:08:00")
        val focusStudyTime: List<FocusStudyTime> = listOf(focusTime,focusStudyTime1,focusStudyTime3,focusStudyTime4,focusStudyTime5)
        val maxFocusStudyTime = TimeCalculator().stringToLong("00:30:00")
        val realStudyTime = TimeCalculator().stringToLong("01:30:00")
        val totalStudyTime = realStudyTime+10000
        val result = Result(focusStudyTime,maxFocusStudyTime,realStudyTime,totalStudyTime)
        destination.setValue(result)


    }

    private fun updateLabel() {
        val myFormat = "yyyy.MM.dd"
        val sdf = SimpleDateFormat(myFormat, Locale.KOREA)
        calendarText.text = sdf.format(calendar.time)
    }
}