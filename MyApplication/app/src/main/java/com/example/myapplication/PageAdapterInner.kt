package com.example.myapplication

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.myapplication.tabviewpager.FragmentTabCalendar
import com.example.myapplication.tabviewpagerinner.FragmentTabDaily
import com.example.myapplication.tabviewpagerinner.FragmentTabMonthly
import com.example.myapplication.tabviewpagerinner.FragmentTabWeekly

class PageAdapterInner(fragmentActivity: FragmentTabCalendar):
    FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> FragmentTabDaily()
            1 -> FragmentTabMonthly()
            else -> FragmentTabWeekly()
        }
    }

}
