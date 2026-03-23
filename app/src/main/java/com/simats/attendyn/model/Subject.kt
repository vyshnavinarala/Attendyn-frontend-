package com.simats.attendyn.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Subject(
    val id: Int? = null,
    val name: String,
    var conducted: Int,
    var attended: Int,
    var history: MutableMap<String, Int>? = mutableMapOf() // status per date
) : Parcelable {
    val percentage: Double
        get() = if (conducted > 0) (attended.toDouble() / conducted.toDouble()) * 100.0 else 0.0

    val attendanceHistory: MutableMap<String, Int>
        get() {
            if (history == null) history = mutableMapOf()
            return history!!
        }
}
