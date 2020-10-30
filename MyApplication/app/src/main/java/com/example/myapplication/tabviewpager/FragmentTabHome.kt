package com.example.myapplication.tabviewpager

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.myapplication.CustomDialog
import com.example.myapplication.R
import com.example.myapplication.TimerActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import kotlinx.android.synthetic.main.layout_home.view.*
import java.util.Calendar
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_timer.*

class FragmentTabHome  : Fragment() {
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
        val date = "$year$month$day"
        val uid = FirebaseAuth.getInstance().uid
        val ref =FirebaseDatabase.getInstance().getReference("/calendar/$uid/$date/result")
        var time: Long= 0

        ref.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("파베","일단 들어옴")

                    if (snapshot.key.equals("result")) {
                        val cal = snapshot.getValue(Cal::class.java)
                        if(cal!=null)
                        {

                            Log.d("파베1",""+cal.totalstudytime)
                            time = cal.totalstudytime
                            view.chronometer.base= SystemClock.elapsedRealtime()+time;
                        }
                    }

            }
        })
        view.btnStart.setOnClickListener {
            val intent = Intent(context, TimerActivity::class.java)

            intent.putExtra("time",time)
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
data class Cal(
    var totalstudytime: Long = 0,
    var realstudytime: Long = 0,
    var maxfocusstudytime: Long = 0
)