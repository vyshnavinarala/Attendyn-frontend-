package com.simats.attendyn.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: Int,
    val name: String,
    val email: String,
    @SerializedName("attendance_goal") val attendanceGoal: Int?,
    @SerializedName("photo_path") val photoPath: String?
)
