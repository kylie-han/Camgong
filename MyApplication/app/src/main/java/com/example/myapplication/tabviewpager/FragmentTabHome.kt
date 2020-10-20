package com.example.myapplication.tabviewpager

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.myapplication.R
import com.example.myapplication.TimerActivity
import kotlinx.android.synthetic.main.layout_home.view.*
import java.util.Calendar

class FragmentTabHome  : Fragment() {
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH) +1
    val date = calendar.get(Calendar.DATE)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view =inflater.inflate(R.layout.layout_home, container, false)
        view.homeDate.text = "$year.$month.$date"
        view.btnStart.setOnClickListener {
            val intent = Intent(context, TimerActivity::class.java)
            AlertDialog.Builder(context)
                .setMessage("캠 스터디를 시작하시겠습니까?")
                .setPositiveButton("OK",
                    DialogInterface.OnClickListener { dialog, which -> startActivity(intent) })
                .setNegativeButton("CANCEL", null)
                .show()


        }

        return view
    }


}