package com.simats.attendyn.model

import com.google.gson.annotations.SerializedName

data class VerifyOtpRequest(
    @SerializedName("email") val email: String,
    @SerializedName("otp") val otp: String
)
