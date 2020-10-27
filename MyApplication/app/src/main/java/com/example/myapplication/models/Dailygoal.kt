package com.example.myapplication.models

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Dailygoal(var goalstatus: Boolean, var goaltime: String){
}
