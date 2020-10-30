package com.example.myapplication.tabviewpager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.MPPointF
import kotlinx.android.synthetic.main.layout_calendar.view.*


class FragmentTabCalendar : Fragment() {
    private val tabTextList = arrayListOf("Calendar", "HOME", "STATS")


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.layout_calendar, container, false)
        val pieChart = view.piechart
        val time = ArrayList<PieEntry>()
        val listColors = ArrayList<Int>()
        
        // 여기 value에 데이터 값 넣어주세요
        time.add(PieEntry(3f, "공부"))
        listColors.add(resources.getColor(R.color.btnBackColor))
        time.add(PieEntry(7f, "휴식"))
        listColors.add(resources.getColor(R.color.cancelBtnBackColor))

        val dataSet = PieDataSet(time, "");
        dataSet.colors = listColors
        dataSet.setDrawIcons(false)
        dataSet.sliceSpace = 3f
        dataSet.iconsOffset = MPPointF(0F, 40F)
        dataSet.selectionShift = 5f
        dataSet.valueFormatter = PercentFormatter(pieChart)

        val pieData = PieData(dataSet)
        pieData.setValueTextSize(17f)
        pieChart.data = pieData
        pieChart.setUsePercentValues(true)
        pieChart.highlightValues(null)
        pieChart.invalidate()
        pieChart.description.isEnabled = false
        pieChart.animateXY(1000, 1000);

        return view
    }

}