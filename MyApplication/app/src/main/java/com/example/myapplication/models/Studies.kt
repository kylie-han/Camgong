package com.example.myapplication.models

data class Studies(
    var studies: List<Study> = emptyList()
)

data class Study(
    var startTime: String = "",
    var endTime: String = "",
    var realStudy: List<RealStudy> = emptyList()
)

data class RealStudy(
    var realStudyStartTime: String = "",
    var realStudyEndTime: String = ""
)
