package com.example.myapplication.models

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Result (
    var focusStudyTime: Map<String,FocusStudyTime> = emptyMap(),
    var maxFocusStudyTime: String = "",
    var realStudyTime: String = "",
    var totalStudyTime: Long = 0L
){
    @Exclude
    fun toMap() : Map<String, Any?> {
        return  mapOf(
            "focusStudyTime" to focusStudyTime,
            "maxFocusStudyTime" to maxFocusStudyTime,
            "realStudyTime" to realStudyTime,
            "totalStudyTime" to totalStudyTime
        )
    }
}
data class FocusStudyTime(
    var startTime: String = "",
    var endTime:String = ""
)