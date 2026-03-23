package com.simats.attendyn.model

import com.google.gson.annotations.SerializedName

data class GoalRequest(
    @SerializedName("attendance_goal") val attendanceGoal: Int
)
