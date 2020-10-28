package com.example.myapplication.models

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Dailygoal(
    var goalStatus: Boolean = false,
    var goalTime: String = ""
)
