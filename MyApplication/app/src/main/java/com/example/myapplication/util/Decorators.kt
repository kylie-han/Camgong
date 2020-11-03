package com.example.myapplication.util

import android.graphics.Color
import android.text.style.ForegroundColorSpan
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import java.util.*

// 토,일요일날 각각 파란색, 빨간색으로 표시하기 위한 데코레이터
public class SundayDecorator : DayViewDecorator {
    private val calendar: Calendar = Calendar.getInstance();

    override fun shouldDecorate(day: CalendarDay): Boolean {
        day.copyTo(calendar)
        var weekDay = calendar.get(Calendar.DAY_OF_WEEK)
        return weekDay == Calendar.SUNDAY
    }

    override fun decorate(view: DayViewFacade) {
        view.addSpan(ForegroundColorSpan(Color.RED))
    }
}

public class SaturdayDecorator : DayViewDecorator {
    private val calendar: Calendar = Calendar.getInstance();

    override fun shouldDecorate(day: CalendarDay): Boolean {
        day.copyTo(calendar)
        var weekDay = calendar.get(Calendar.DAY_OF_WEEK)
        return weekDay == Calendar.SATURDAY
    }

    override fun decorate(view: DayViewFacade) {
        view.addSpan(ForegroundColorSpan(Color.BLUE))
    }
}