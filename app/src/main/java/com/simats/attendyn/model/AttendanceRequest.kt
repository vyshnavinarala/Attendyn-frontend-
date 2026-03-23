package com.simats.attendyn.model

import com.google.gson.annotations.SerializedName

data class AttendanceRequest(
    @SerializedName("subject_id") val subjectId: Int,
    @SerializedName("status") val status: Int,
    @SerializedName("date") val date: String? = null
)
