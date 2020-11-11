package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_calendar.*
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    private val tabTextList = arrayListOf("Calendar", "HOME", "STATS")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()

//        tabs.setScrollPosition(1,0f,true)
        view_pager.setCurrentItem(1)
    }

    private fun init() {
        view_pager.adapter = PageAdapter(this)
        TabLayoutMediator(tabs, view_pager) {
                tab, position ->
            tab.setIcon(R.mipmap.ic_new_launcher)
            tab.text = tabTextList[position]
        }.attach()
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> CustomDialog(this)
                .setTitle("종료하시겠습니까")
                .setMessage("지켜보고있다")
                .setPositiveButton("OK") { finish()
                }.setNegativeButton("CANCEL") {null
                }.show()
        }
        return true
    }
}