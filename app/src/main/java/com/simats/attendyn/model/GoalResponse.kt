package com.simats.attendyn.model

import com.google.gson.annotations.SerializedName

data class GoalResponse(
    @SerializedName("message") val message: String?,
    @SerializedName("error") val error: String?
)
