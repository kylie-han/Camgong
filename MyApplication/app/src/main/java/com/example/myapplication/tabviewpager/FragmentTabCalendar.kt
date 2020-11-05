
package com.example.myapplication.tabviewpager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.models.DailyGoal
import com.example.myapplication.models.Result
//import com.example.myapplication.util.AchiveDecorator
//import com.example.myapplication.util.SaturdayDecorator
//import com.example.myapplication.util.SundayDecorator
import com.example.myapplication.util.*
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.MPPointF
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
    private val tabTextList = arrayListOf("Calendar", "HOME", "STATS")
    var totalTime = 0L
    var focusTime = 0L
    var realTime = 0L
    var breakTime = 0L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.layout_calendar, container, false)

        // 캘린더 초기 설정
        initCalendar(view)

        // 목표치 달성했는지 판단하여 달력에 표시하는메서드
        isAchived(view, CalendarDay.today())
        getMonthInfo(view, CalendarDay.today())

        // 선택된 날짜가 변경 되었을때
        view.calendar.setOnDateChangedListener(object : OnDateSelectedListener{
            override fun onDateSelected(widget: MaterialCalendarView, date: CalendarDay, selected: Boolean) {

                // 파이어베이스에서 값 불러와서 textView에 표시 + 비율을 받아서 차트 그리기
                getDayInfo(view, date)

                getWeekInfo(view, date)
            }
        })// end of setOnDateChangedListener


        // 캘린더 월 변경 리스너 -> isAchived() 호출
        view.calendar.setOnMonthChangedListener(object : OnMonthChangedListener{
            override fun onMonthChanged(widget: MaterialCalendarView, date: CalendarDay) { // 달이 변경되었을때
                isAchived(view, date) // 목표 달성여부 달력에 표시

                totalTime = 0; realTime = 0; focusTime = 0;
                getMonthInfo(view, date) // 해당 월의 공부정보 가져옴
            }
        })// end of setOnMonthChangedListener

        return view
    }

    // 주별 확인
    private fun getWeekInfo(view: View, calendarDay: CalendarDay) {
        // 요일 확인하여 그 주의 일요일에해당하는 날짜 선택 (firstDayOfWeek가 계속 1로 나오네...)
        var DoW = calendarDay.calendar.DAY_OF_WEEK
    }

    private fun displayMonth(view: View) {
        if(totalTime == 0L)
            view.tv_month.text = "한달동안 공부한 내역이 없습니다."
        else{
            val tc = TimeCalculator()
            breakTime = totalTime - realTime
            var str = "총 시간: ${tc.msToStringTime(totalTime)}\n"
            str += "실제 시간: ${tc.msToStringTime(realTime)}\n"
            str += "최대 집중시간: ${tc.msToStringTime(focusTime)}\n"
            str += "휴식 시간: ${tc.msToStringTime(breakTime)}\n"
            str += "공부 비율: ${tc.percentage(realTime, totalTime)}%"

            view.tv_month.text = str
        }
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
            else res += "0${date.day}"
        }

        return res
    }

    // 공부비율과 휴식비율을 받아서 차트를 만듦
    private fun drawPieChart(view: View, realTime: Long, breakTime: Long) {
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
        pieChart.animateXY(1000, 1000);
    }

    // 1일 공부정보 가져옴 -> 모델 변경 필요
    fun getDayInfo(view: View, calendarDay: CalendarDay) {
        val uid = FirebaseAuth.getInstance().uid
        val date = getStringDate(calendarDay, true)
        val ref = FirebaseDatabase.getInstance().getReference("/calendar/$uid/$date/result")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // result부분 가져오기
                if(snapshot.key.equals("result")){
                    // DB에서 해당 값 가져오기, ResultTime 클래스에 맞게 자동으로 저장됨
                    val data = snapshot.getValue(Result::class.java)

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
                        drawPieChart(view, realTime, breakTime)
                    }else{ // 정보가 없을경우 모든 값을 없음으로 처리
                        view.tv_calendar.text = "정보가 없음"
                    }
                }// end of outer if()
            }

            override fun onCancelled(error: DatabaseError) {
                view.tv_calendar.text = "Failed"
                println("Failed to read Value")
            }

        })

    } // end of getDate()

    // 1달의 공부 정보를 가져와서 표시
    private fun getMonthInfo(view: View, current: CalendarDay) {
        val uid = FirebaseAuth.getInstance().uid
        val ins = FirebaseDatabase.getInstance()
        val month = getStringDate(current)

        val tc = TimeCalculator()
        for (i in 1..31){
            var date = month
            if(i<10) date += "0$i"
            else date += "$i"
            var ref = ins.getReference("/calendar/$uid/$date/result")

            ref.addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.key.equals("result")){
                        val data = snapshot.getValue(Result::class.java)

                        if(data != null){
                            totalTime += data.totalStudyTime
                            focusTime += data.maxFocusStudyTime
                            realTime += data.realStudyTime
                            breakTime += data.totalStudyTime - data.realStudyTime
                            displayMonth(view)
                        }
                    }
                }// end of onDataChange

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(view.context, "월별 계산 실패\n$date", Toast.LENGTH_SHORT).show()
                }
            })
        }// end of for
    }// end of getMonthInfo()

    // 각 일자의 목표 달성여부를 판단하여 달력에 표시 -> 미완성
    // 실행되면 1~31일 훑어서 goalstatus 값에 따라 잘 가져오는것 까지 확인하였음 -> 값에따라 캘린더의 해당날짜에 표시
    private fun isAchived(view: View, current: CalendarDay) {
        val uid = FirebaseAuth.getInstance().uid
        val ins = FirebaseDatabase.getInstance()
        val month = getStringDate(current)
        for (i in 1..31){
            var date = month  // YYYYMMDD
            if(i<10) date += "0$i"
            else date += "$i"

            var ref = ins.getReference("/calendar/$uid/$date/dailyGoal")
            val day = CalendarDay.from(current.year, current.month, i) // 현재 조회중인 날짜

            ref.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.key.equals("dailyGoal")){
                        val data = snapshot.getValue(DailyGoal::class.java)
                        var str = "기본값"
                        if(data != null){ // 목표가 설정되어있을경우
                            if(data.goalStatus){ // 달성한 경우
                                // 파란색으로 해당날짜에 표시
                                view.calendar.addDecorators(AchiveDecorator(day,1))
                            }else{ // 달성 못한 경우
                                // 빨간색으로 해당날짜에 표시
                                view.calendar.addDecorators(AchiveDecorator(day,2))
                            }
                        }else{ // 목표가 없는 경우, 회색으로 해당날짜에 표시(삭제됨)
//                            view.tv_calendar.text = "$str" + date+"목표 없음             "
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(view.context, "불러오기 실패\n$date", Toast.LENGTH_SHORT).show()
                }

            })
        }// end of for
    } // end of isAchived()

}