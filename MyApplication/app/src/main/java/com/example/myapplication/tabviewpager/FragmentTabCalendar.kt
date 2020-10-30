package com.example.myapplication.tabviewpager

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.applikeysolutions.cosmocalendar.listeners.OnMonthChangeListener
import com.applikeysolutions.cosmocalendar.model.Month
import com.applikeysolutions.cosmocalendar.selection.OnDaySelectedListener
import com.applikeysolutions.cosmocalendar.selection.SingleSelectionManager
import com.applikeysolutions.cosmocalendar.view.CalendarView
import com.example.myapplication.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.layout_calendar.view.*
import java.text.SimpleDateFormat
import java.util.*

class FragmentTabCalendar : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.layout_calendar, container, false)
        view.calendar.isShowDaysOfWeekTitle = false
        var calendarView: CalendarView = view.calendar as CalendarView

        // 목표치 달성했는지 판단하여 달력에 표시하는메서드
        isAchived()

        // 캘린더 월 변경 리스너 -> isAchived() 호출
        view.calendar.setOnMonthChangeListener(object : OnMonthChangeListener {
            override fun onMonthChanged(month: Month?) { // 달이 변경되었을때
//                isAchived()
                Toast.makeText(view.context,"달 변경 됨", Toast.LENGTH_SHORT).show()
            }
        })

        // 선택된 날짜가 변경 되었을때
        view.calendar.selectionManager = SingleSelectionManager(OnDaySelectedListener {
            if (view.calendar.selectedDates.size <= 0)
                return@OnDaySelectedListener
            // 선택된 일자
            var date = selectedDay(calendarView)

            // 파이어베이스에서 값 불러와서 textView에 표시
            getDate(view, date)
        })// end of Listener
        return view
    }

    private fun selectedDay(calendarView: CalendarView): String {
        var selected: List<Calendar> = calendarView.selectedDates
        var date:String = "더미~"

        // 선택된 날짜를 가져옴(선택된것이 있을때만) + 그 주의 공부시간 총합 출력
        if (selected != null) {
            for (cal in selected){ // 1개만 선택할 경우 selected[0]를 cal로 놔둘것
                val year = cal.get(Calendar.YEAR)
                val month = cal.get(Calendar.MONTH)+1
                val day = cal.get(Calendar.DAY_OF_MONTH)
                date = "${year}${month}${day}"
            }
        }
        return date
    }

    // 각 일자의 목표 달성여부를 판단하여 달력에 표시
    private fun isAchived() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/calendar/$uid")
    }

    fun getDate(view: View, date: String) {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/calendar/$uid/$date/result")

        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                // result부분 가져오기
                if(snapshot.key.equals("result")){
                    // DB에서 해당 값 가져오기, ResultTime 클래스에 맞게 자동으로 저장됨
                    val cal = snapshot.getValue(ResultTime::class.java)

                    if(cal != null){
                        // 받아온 값 그대로 출력
                        view.tv_maxFocusTime.text = "${cal.maxfocusstudytime}"
                        view.tv_studyTime.text = "${cal.realstudytime}"
                        view.tv_totalTime.text = "${cal.totalstudytime}"

                        // Parsing할때 사용할 포맷 지정
                        val format = SimpleDateFormat("HH:mm:ss")

                        // 시간 계산을 위해 String -> Date 형식으로 Parsing 1970.01.01로부터 지난 시간을 각각 저장
                        val total = format.parse(cal.totalstudytime).time // 총 공부시간(ms)
                        val real = format.parse(cal.realstudytime).time // 실제 공부시간(ms)
                        val zero = format.parse("00:00:00").time // 순수한 시간 계산을 위한 기준값


                        val breakTime = total - real // 휴식시간 (ms)
                        view.tv_breakTime.text = "${breakTime / (1000*60)}분" // 분으로 출력

                        // 휴식, 공부 비율 계산, toDouble은 소수점을 출력하기 위해서 사용하였음
                        val breakRatio = (breakTime.toDouble() / (total - zero) * 100).toInt()
                        val studyRatio = 100 - breakRatio
                        view.tv_studtyRatio.text = "공부 비율: ${studyRatio}%"
                        view.tv_breakRatio.text = "휴식 비율: ${breakRatio}%"
                    }else{ // 정보가 없을경우 모든 값을 없음으로 처리
                        view.tv_breakTime.text = "없음"
                        view.tv_maxFocusTime.text = "없음"
                        view.tv_studtyRatio.text = "없음"
                        view.tv_breakRatio.text = "없음"
                        view.tv_studyTime.text = "없음"
                        view.tv_totalTime.text = "없음"
                    }
                }// end of outer if()
            }

            override fun onCancelled(error: DatabaseError) {
                view.tv_studtyRatio.text = "Failed"
                println("Failed to read Value")
            }

        })

    } // end of getDate()
}

data class ResultTime(
    var totalstudytime: String = "",
    var realstudytime: String = "",
    var maxfocusstudytime: String = ""
)