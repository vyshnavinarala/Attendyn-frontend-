package com.simats.attendyn.model

import com.google.gson.annotations.SerializedName

data class SubjectRequest(
    @SerializedName("name") val name: String,
    @SerializedName("conducted") val conducted: Int,
    @SerializedName("attended") val attended: Int
)
