package com.example.myapplication.util

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.myapplication.models.Focusstudytime
import java.time.*
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.time.milliseconds

@RequiresApi(Build.VERSION_CODES.O)
class TimeCalculator {
    // 시간 계산
    private val current = LocalDateTime.now(ZoneId.of("Asia/Seoul"))
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    /**
     * today() : 오늘 날짜
     * currentTime : 현재시간
     * subTime : 두 시각 사이의 시간 계산
     * addTime : 시간 List를 모두 더함
     * dateToString : LocalDate의 날짜를 String으로 변환
     * timeToString : LocalTime의 시간을 String으로 변환
     * stringToDate : String형의 날짜를 LocalDate형으로 변환
     * stringToTime :
     * msToStringTime : Long형의 millisecond를 String형 시간(시:분:초)으로 변환
     */
    fun today(): String? {
        return current.format(dateFormatter)
    }
    fun currentTime(): String{
        return current.format(timeFormatter)
    }
    fun subTime(startTime: String, endTime: String): String? {
        val start = LocalTime.parse(startTime, timeFormatter)
        val end = LocalTime.parse(endTime, timeFormatter)
        val absSeconds = abs(Duration.between(start,end).seconds)
        return String.format(
            "%02d:%02d:%02d",
            absSeconds / 3600,
            absSeconds % 3600 / 60,
            absSeconds % 60
        )
    }
    fun addTime(times: Map<String,Focusstudytime>){


    }
    fun dateToString(date: LocalDate): String{
        return date.format(dateFormatter)
    }
    fun timeToString(time: LocalTime): String{
        return time.format(timeFormatter)
    }
    fun stringToDate(date: String): LocalDate {
        return LocalDate.parse(date, dateFormatter)
    }
    fun stringToTime(time: String): LocalTime {
        return LocalTime.parse(time, timeFormatter)
    }
    fun msToStringTime(milliSec: Long): String{
        return String.format(
            "%02d:%02d:%02d.%03d",
            milliSec / (60*60),
            milliSec % (60*60) / 60,
            milliSec % 60
        )
//        return String.format(
//            "%02d:%02d:%02d.%03d",
//            milliSec / (60*60*1000),
//            milliSec % (60*60*1000) / 60,
//            milliSec % (60*60*1000) / (60*60),
//            milliSec % 1000
//        )
    }
}

