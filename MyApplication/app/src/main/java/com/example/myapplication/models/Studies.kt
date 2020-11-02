package com.example.myapplication.models

data class Studies(
    var studies: Map<String,Study> = emptyMap()
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
