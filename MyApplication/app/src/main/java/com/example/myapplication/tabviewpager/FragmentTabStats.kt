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
import com.example.myapplication.models.*
import com.example.myapplication.util.TimeCalculator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.layout_stats.*
import kotlinx.android.synthetic.main.layout_stats.view.*
import java.lang.ref.Reference
import java.text.SimpleDateFormat
import java.util.*


// DATE class를 사용하기 위한 API제한
class FragmentTabStats : Fragment() {
    // database에 연결
    private lateinit var database: DatabaseReference
    // Log의 TAG
    private val TAG = "FragmentTabStats"
    var calendar: Calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH) +1
    val date = calendar.get(Calendar.DATE)
    var resultValueEventListener: ValueEventListener? = null
    var dailyGoalValueEventListener: ValueEventListener? = null
    var studiesValueEventListener: ValueEventListener? = null

    val studies = Studies()

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
            updateData(database,uid)
            view.calendarText.text = "$year.$month.$date"

            val tv_date = view.calendarText

            var myDatePicker =
                OnDateSetListener { view, year, month, dayOfMonth ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    updateLabel()
                    updateData(database, uid)
                }

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
        val focusStudyTime: MutableList<FocusStudyTime> = mutableListOf(focusTime)
        val maxFocusStudyTime = TimeCalculator().stringToLong("00:30:00")
        val realStudyTime = TimeCalculator().stringToLong("01:30:00")
        val totalStudyTime = realStudyTime+10000
        val result = Result(focusStudyTime,maxFocusStudyTime,realStudyTime,totalStudyTime)
        destination.setValue(result)
    }

    private fun studyWrite(myRef: DatabaseReference){
        val destination = myRef.child("/studies")
        val realstart = "13:30:00"
        val realend = "13:50:00"
        val realStudy = RealStudy(realstart, realend)
        val startTime = "13:00:00"
        val endTime = "14:00:00"
        val study = Study(startTime,endTime, mutableListOf(realStudy))
        var studies: Studies = Studies(mutableListOf(study))
        destination.setValue(studies)
    }

    private fun updateLabel() {
        val myFormat = "yyyy.MM.dd"
        val sdf = SimpleDateFormat(myFormat, Locale.KOREA)
        calendarText.text = sdf.format(calendar.time)
    }
    private fun updateData(database: FirebaseDatabase,uid: String) {
        val myFormat = "yyyyMMdd"
        val sdf = SimpleDateFormat(myFormat,Locale.KOREA)
        val day = sdf.format(calendar.time)
        Log.e(TAG,"$day")
        val myRef = database.getReference("calendar/$uid/$day")

        if(resultValueEventListener != null) myRef.removeEventListener(resultValueEventListener!!)
        if(dailyGoalValueEventListener != null) myRef.removeEventListener(dailyGoalValueEventListener!!)
        if(studiesValueEventListener != null) myRef.removeEventListener(studiesValueEventListener!!)
        // [START read_message]
        resultValueEventListener =
            myRef.child("/result").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val value = snapshot.getValue<Result>()
                    if(value == null){
                        Log.d(TAG,"Result가 없음")
                        timeText.text = "00:00:00"
                        realTime.text = "00:00:00"
                        recommendTime.text = "00:00:00"
                        maxFocusTime.text = "00:00:00"
                    }else {
                        val total = TimeCalculator().msToStringTime(value.totalStudyTime).substring(0,8)
                        // 총 공부 시간
                        timeText.text = "$total"
                        // 실제 공부한 시간
                        val real = TimeCalculator().msToStringTime(value.realStudyTime).substring(0,8)
                        realTime.text = "$real"
                        // 공부에 집중한 시간
                        var list = value.focusStudyTime.toList()
                        var string:String = ""
                        list = list.sortedWith(Comparator { data1, data2 ->
                            (TimeCalculator().stringToLong(data2.endTime)-TimeCalculator().stringToLong(data2.startTime))
                                .compareTo((TimeCalculator().stringToLong(data1.endTime))-TimeCalculator().stringToLong(data1.startTime))
                        })

                        for (i in list.indices){
                            if(i == 3)break
                            string += "${list[i].startTime} ~ ${list[i].endTime}\n"
                        }
                        recommendTime.text = "${string}"
                        //최대 공부 시간 : maxFocusStudyTime
                        val max = TimeCalculator().msToStringTime(value.maxFocusStudyTime).substring(0,8)
                        maxFocusTime.text = "${max}"

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read value.", error.toException())
                }
            })
        dailyGoalValueEventListener =
            myRef.child("/dailyGoal").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val value = snapshot.getValue<DailyGoal>()
                    if (value == null) {
                        Log.d(TAG, "DailyGoal이 없음")
                        goalTime.text = "00:00:00"
                    } else {
                        val goal = TimeCalculator().msToStringTime(value.goalTime).substring(0,8)
                        goalTime.text = "$goal"
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    // Failed to read value
                    Log.w(TAG, "Failed to read value.", error.toException())
                }
            })
        studiesValueEventListener =
            myRef.child("/studies").addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val value = snapshot.getValue<Studies>()
                    if(value == null){
                        Log.d(TAG,"Studies가 없음")
                    }else {
                        val studies = value.studies
                        // list : 00:00:00 ~23:59:59
                        // 10분단위로 체크
                        // 0시 0분-0시 10분 : tr0_td1
                        for (study in studies){
                            val startHour = study.startTime.substring(0,2)  // 00~23 시간
                            val startMin = (study.startTime.substring(3,5).toInt()+5)/10+1    // 0~4 : td1, 5~14 : td2, 55~59 : X
                            val endHour = study.endTime.substring(0,2)
                            val endMin = (study.endTime.substring(3,5).toInt()) //0~4 : X, 5~14 : td
                            val real: MutableList<RealStudy> = study.realStudy

                        }
                        Log.d(TAG,value.studies.toString())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w(TAG,"Failed to read value.",error.toException())
                }
            })
        // [END read_message]
    }

}