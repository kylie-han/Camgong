package com.example.myapplication.tabviewpager

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.os.SystemClock
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.myapplication.CustomDialog
import com.example.myapplication.R
import com.example.myapplication.TimerActivity
import com.example.myapplication.models.FocusStudyTime
import com.example.myapplication.models.RealStudy
import com.example.myapplication.models.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import kotlinx.android.synthetic.main.layout_home.view.*
import java.util.Calendar
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_timer.*
import java.util.ArrayList


class FragmentTabHome  : Fragment() {
    private var timer : Result = Result()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view =inflater.inflate(R.layout.layout_home, container, false)
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) +1
        val day = calendar.get(Calendar.DATE)
        view.homeDate.text = "$year.$month.$day"
        var date = "$year"
        if(month<10)
        {
            date+="0$month"
        }else
        {
            date+="$month"
        }
        if(day<10)
        {
            date+="0$day"
        }else
        {
            date+="$day"
        }
        val uid = FirebaseAuth.getInstance().uid
        val ref =FirebaseDatabase.getInstance().getReference("/calendar/$uid/$date/result")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.key.equals("result")) {
                        val result = snapshot.getValue(Result::class.java)
                        if(result!=null)
                        {
                            timer = Result(result.focusStudyTime,result.maxFocusStudyTime,result.realStudyTime,result.totalStudyTime)
                            view.chronometer.base= SystemClock.elapsedRealtime()+timer.realStudyTime
                        }
                    }

            }
        })
        view.btnStart.setOnClickListener {
            val intent = Intent(context, TimerActivity::class.java)
            /*
            intent.putParcelableArrayListExtra("focusStudyTime", ArrayList(timer.focusStudyTime))
            intent.putExtra("maxFocusStudyTime",timer.maxFocusStudyTime)
            intent.putExtra("realStudyTime",timer.realStudyTime)
            intent.putExtra("totalStudyTime",timer.totalStudyTime)*/
            context?.let { it1 ->
                CustomDialog(it1)
                    .setMessage("캠 스터디를 시작하시겠습니까?")
                    .setPositiveButton("OK") {
                        startActivity(intent)
                    }.setNegativeButton("CANCEL") {
                        null
                    }.show()
            }


        }

        return view
    }
}
