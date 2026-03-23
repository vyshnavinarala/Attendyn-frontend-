package com.simats.attendyn.model

data class RegisterResponse(
    val message: String?,
    val token: String?,
    val user: User?,
    val error: String?
)
