
package com.example.myapplication.tabviewpager

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.applikeysolutions.cosmocalendar.listeners.OnMonthChangeListener
import com.applikeysolutions.cosmocalendar.model.Month
import com.applikeysolutions.cosmocalendar.selection.OnDaySelectedListener
import com.applikeysolutions.cosmocalendar.selection.SingleSelectionManager
import com.applikeysolutions.cosmocalendar.view.CalendarView
import com.example.myapplication.PageAdapterInner
import com.example.myapplication.R
import com.example.myapplication.models.DailyGoal
import com.example.myapplication.models.Result
import com.example.myapplication.util.*
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.MPPointF
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.prolificinteractive.materialcalendarview.*
import kotlinx.android.synthetic.main.layout_calendar.view.*
import java.util.*
import kotlin.collections.ArrayList


class FragmentTabCalendar : Fragment() {
    private val tabTextList = arrayListOf("일간", "주간", "월간")


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.layout_calendar, container, false)

        // 캘린더 초기 설정
        initCalendar(view)

        // 목표치 달성했는지 판단하여 달력에 표시하는메서드
        isAchived(view, CalendarDay.today())

        // 일, 주, 월 공부시간 정보 출력
        getDayInfo(view, CalendarDay.today())
        getWeekInfo(view, CalendarDay.today())
        getMonthInfo(view, CalendarDay.today())

        // 선택된 날짜가 변경 리스너
        view.calendar.setOnDateChangedListener(object : OnDateSelectedListener{
            override fun onDateSelected(widget: MaterialCalendarView, date: CalendarDay, selected: Boolean) {

                // 파이어베이스에서 값 불러와서 textView에 표시 + 비율을 받아서 차트 그리기
                getDayInfo(view, date)

                for (i in 0..2)
                    weekInfo[i] = 0

                getWeekInfo(view, date)
            }
        })// end of setOnDateChangedListener

        // 캘린더 월 변경 리스너 -> isAchived() 호출
        view.calendar.setOnMonthChangedListener(object : OnMonthChangedListener{
            override fun onMonthChanged(widget: MaterialCalendarView, date: CalendarDay) { // 달이 변경되었을때
                isAchived(view, date) // 목표 달성여부 달력에 표시

                // 달이 바뀌면 일, 주별 출력정보 + 차트는 초기화 시킴
                view.tv_calendar.text = "없음"
                for (i in 0..2){
                    weekInfo[i] = 0
                    monthInfo[i] = 0
                }
//                drawDayPieChart(view, 0, 0)
                drawWeekPieChart(view, 0, 0)
                getMonthInfo(view, date) // 해당 월의 공부정보 가져옴
            }
        })// end of setOnMonthChangedListener

        return view
    }

    private fun initCalendar(view: View) {
        // 캘린더 선택설정
        view.calendar.selectionMode = MaterialCalendarView.SELECTION_MODE_SINGLE

        // 캘린더 기본설정
        view.calendar.state().edit()
            .setFirstDayOfWeek(Calendar.SUNDAY)
            .setMinimumDate(CalendarDay.from(2000,0,1))
            .setMaximumDate(CalendarDay.from(2100,11,31))
            .setCalendarDisplayMode(CalendarMode.MONTHS)
            .commit()

        // 달력 꾸미기(토/일요일 색상 변경)
        view.calendar.addDecorators(
            SundayDecorator(),
            SaturdayDecorator()
        )
        view.calendar.setDateSelected(CalendarDay.today(), true)
    }

    // calendarDay를 받아서 기본 YYYYMM, true일 경우 YYYYMMDD
    private fun getStringDate(date: CalendarDay, flag: Boolean = false): String {
        var res : String = "${date.year}"

        if(date.month < 10) res += "0${date.month+1}"
        else res += "${date.month+1}"

        if(flag){
            if(date.day < 10) res += "0${date.day}"
            else res += "${date.day}"
        }

        return res
    }

    // 일별공부비율과 휴식비율을 받아서 차트를 만듦
//    private fun drawDayPieChart(view: View, realTime: Long, breakTime: Long) {
//        val pieChart = view.piechart
//        val time = ArrayList<PieEntry>()
//        val listColors = ArrayList<Int>()
//
//        // 여기 value에 데이터 값 넣어주세요
//        time.add(PieEntry(realTime.toFloat(), "공부"))
//        listColors.add(resources.getColor(R.color.btnBackColor))
//        time.add(PieEntry(breakTime.toFloat(), "휴식"))
//        listColors.add(resources.getColor(R.color.cancelBtnBackColor))
//
//        val dataSet = PieDataSet(time, "");
//        dataSet.colors = listColors
//        dataSet.setDrawIcons(false)
//        dataSet.sliceSpace = 3f
//        dataSet.iconsOffset = MPPointF(0F, 40F)
//        dataSet.selectionShift = 5f
//        dataSet.valueFormatter = PercentFormatter(pieChart)
//
//        val pieData = PieData(dataSet)
//        pieData.setValueTextSize(17f)
//        pieChart.data = pieData
//        pieChart.setUsePercentValues(true)
//        pieChart.highlightValues(null)
//        pieChart.invalidate()
//        pieChart.description.isEnabled = false
//        pieChart.animateXY(1000, 1000);
//    }
 
    // 주별 공부비율과 휴식비율을 받아서 차트를 만듦
    private fun drawWeekPieChart(view: View, realTime: Long, breakTime: Long) {
        val pieChart = view.piechart
        val time = ArrayList<PieEntry>()
        val listColors = ArrayList<Int>()

        // 여기 value에 데이터 값 넣어주세요
        time.add(PieEntry(realTime.toFloat(), "공부"))
        listColors.add(resources.getColor(R.color.btnBackColor))
        time.add(PieEntry(breakTime.toFloat(), "휴식"))
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
        pieChart.setRotationEnabled(false)
        pieChart.animateXY(1000, 1000);

        // 선택된 날짜가 변경 되었을때
        view.calendar.selectionManager = SingleSelectionManager(OnDaySelectedListener {
            if (view.calendar.selectedDates.size <= 0)
                return@OnDaySelectedListener
            // 선택된 일자
            var date = selectedDay(calendarView)

            // 파이어베이스에서 값 불러와서 textView에 표시
            getDate(view, date)
        })// end of Listener

        innerInit(view)

        return view
    }

    // 월별공부비율과 휴식비율을 받아서 차트를 만듦
//    private fun drawMonthPieChart(view: View, realTime: Long, breakTime: Long) {
//        val pieChart = view.piechart
//        val time = ArrayList<PieEntry>()
//        val listColors = ArrayList<Int>()
//
//        // 여기 value에 데이터 값 넣어주세요
//        time.add(PieEntry(realTime.toFloat(), "공부"))
//        listColors.add(resources.getColor(R.color.btnBackColor))
//        time.add(PieEntry(breakTime.toFloat(), "휴식"))
//        listColors.add(resources.getColor(R.color.cancelBtnBackColor))
//
//        val dataSet = PieDataSet(time, "");
//        dataSet.colors = listColors
//        dataSet.setDrawIcons(false)
//        dataSet.sliceSpace = 3f
//        dataSet.iconsOffset = MPPointF(0F, 40F)
//        dataSet.selectionShift = 5f
//        dataSet.valueFormatter = PercentFormatter(pieChart)
//
//        val pieData = PieData(dataSet)
//        pieData.setValueTextSize(17f)
//        pieChart.data = pieData
//        pieChart.setUsePercentValues(true)
//        pieChart.highlightValues(null)
//        pieChart.invalidate()
//        pieChart.description.isEnabled = false
//        pieChart.animateXY(1000, 1000);
//    }

    // 1일 공부정보 가져옴
    fun getDayInfo(view: View, calendarDay: CalendarDay) {
        val uid = FirebaseAuth.getInstance().uid
        val date = getStringDate(calendarDay, true)
        val ref = FirebaseDatabase.getInstance().getReference("/calendar/$uid/$date/result")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // result부분 가져오기
                if(snapshot.key.equals("result")){
                    // DB에서 해당 값 가져오기, Result 클래스에 맞게 자동으로 저장됨
                    val data = snapshot.getValue(Result::class.java)
                    Log.e("date", "$date")
                    Log.e("당일 공부 정보", "${data?.realStudyTime}")

                    if(data != null){
                        val tc = TimeCalculator()

                        // 총 공부시간, 실제 공부시간, 최대집중시간, 휴식시간
                        val totalTime = data.totalStudyTime
                        val realTime = data.realStudyTime
                        val focusTime = data.maxFocusStudyTime
                        val breakTime = totalTime - realTime

                        // 값 출력
                        var str = "총 공부시간: ${tc.msToStringTime(totalTime)}\n"
                        str += "실제 공부시간: ${tc.msToStringTime(realTime)}\n"
                        str += "최대 집중 시간: ${tc.msToStringTime(focusTime)}\n"
                        str += "휴식시간: ${tc.msToStringTime(breakTime)}\n"
                        str += "공부시간 비율: ${tc.percentage(realTime,totalTime)}%\n"
                        str += "휴식시간 비율: ${tc.percentage(breakTime,totalTime)}%\n"

                        view.tv_calendar.text = str

                        // 휴식, 공부 비율 그래프로 출력
//                        drawDayPieChart(view, realTime, breakTime)
                    }else{ // 정보가 없을경우 모든 값을 없음으로 처리
                        view.tv_calendar.text = "당일 정보가 없음"
                    }
                }// end of outer if()
            }

            override fun onCancelled(error: DatabaseError) {
                view.tv_calendar.text = "Failed"
                println("Failed to read Value")
            }

        })

    } // end of getDate()


    private fun innerInit(view: View) {
        view.view_pager_in.adapter = PageAdapterInner(this)
        TabLayoutMediator(view.tab_in, view.view_pager_in) {
                tab, position ->
            tab.text = tabTextList[position]
        }.attach()
    }

}

data class ResultTime(
    var totalstudytime: String = "",
    var realstudytime: String = "",
    var maxfocusstudytime: String = ""
)
