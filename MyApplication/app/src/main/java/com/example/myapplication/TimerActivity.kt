package com.example.myapplication

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_timer.*


class TimerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timer)

        btnPause.setOnClickListener {
            AlertDialog.Builder(this)
                .setMessage("기록되었습니다.")
                .setPositiveButton("OK",
                    DialogInterface.OnClickListener { dialog, which -> finish() })
                .show()


        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> AlertDialog.Builder(this)
                .setMessage("기록되었습니다.")
                .setPositiveButton("OK",
                    DialogInterface.OnClickListener { dialog, which -> finish() })
                .show()
        }
        return true
    }
}