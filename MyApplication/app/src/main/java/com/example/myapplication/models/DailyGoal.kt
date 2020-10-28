package com.example.myapplication.models

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class DailyGoal(
    var goalStatus: Boolean = false,
    var goalTime: String = ""
)
