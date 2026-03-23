package com.simats.attendyn.model

import com.google.gson.annotations.SerializedName

data class NameRequest(
    @SerializedName("name") val name: String
)
