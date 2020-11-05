package com.example.myapplication.models

data class Studies(
    var studies: MutableList<Study> = mutableListOf()
)

data class Study(
    var startTime: String = "",
    var endTime: String = "",
    var realStudy: MutableList<RealStudy> = mutableListOf()
)

data class RealStudy(
    var realStudyStartTime: String = "",
    var realStudyEndTime: String = ""
)
