package com.example.myapplication

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.myapplication.tabviewpager.FragmentTabCalendar
import com.example.myapplication.tabviewpager.FragmentTabHome
import com.example.myapplication.tabviewpager.FragmentTabStats

class PageAdapter(fragmentActivity: FragmentActivity):
    FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> FragmentTabCalendar()
            1 -> FragmentTabHome()
            else -> FragmentTabStats()
        }
    }
}