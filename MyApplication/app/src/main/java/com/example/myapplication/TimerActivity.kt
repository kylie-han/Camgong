package com.example.myapplication

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import android.os.SystemClock
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_timer.*
import kotlinx.android.synthetic.main.activity_timer.chronometer
import kotlinx.android.synthetic.main.layout_home.*
import java.util.Calendar


class TimerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)
        var intent = getIntent()
        var time: Long= intent.getLongExtra("time",0)
        chronometer.base=SystemClock.elapsedRealtime()+time;
        chronometer.start();




        btnPause.setOnClickListener {
            val intent = Intent(this, TimerActivity::class.java)
            chronometer.stop();
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH) +1
            val day = calendar.get(Calendar.DATE)
            val date = "$year$month$day"
            val uid = FirebaseAuth.getInstance().uid
            Log.d("타이머",""+uid)
            time = chronometer.base-SystemClock.elapsedRealtime()
            val ref =FirebaseDatabase.getInstance().getReference("/calendar/$uid/$date/result")
            Log.d("time",""+ref)
            ref.child("totalStudyTime").setValue(time)
            Log.d("time",""+time)
            AlertDialog.Builder(this)
                .setMessage("기록되었습니다.")
                .setPositiveButton("OK",
                    DialogInterface.OnClickListener { dialog, which -> finish() })
                .show()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> {AlertDialog.Builder(this)
                .setMessage("기록되었습니다.")
                .setPositiveButton("OK",
                    DialogInterface.OnClickListener { dialog, which -> finish() })
                .show()

            }
        }
        return true
    }
}