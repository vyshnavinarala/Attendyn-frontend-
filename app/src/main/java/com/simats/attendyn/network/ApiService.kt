package com.simats.attendyn.network

import com.simats.attendyn.model.RegisterResponse
import com.simats.attendyn.model.RegisterRequest
import com.simats.attendyn.model.GoalRequest
import com.simats.attendyn.model.GoalResponse
import com.simats.attendyn.model.LoginRequest
import com.simats.attendyn.model.ForgotPasswordRequest
import com.simats.attendyn.model.VerifyOtpRequest
import com.simats.attendyn.model.ResetPasswordRequest
import com.simats.attendyn.model.NameRequest
import com.simats.attendyn.model.ChangePasswordRequest
import com.simats.attendyn.model.Subject
import com.simats.attendyn.model.SubjectRequest
import com.simats.attendyn.model.AttendanceRequest
import com.simats.attendyn.model.CommonResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.DELETE
import retrofit2.http.Path
import retrofit2.http.Multipart
import retrofit2.http.Part
import okhttp3.MultipartBody

interface ApiService {
    @POST("/register")
    fun registerUser(@Body request: RegisterRequest): Call<RegisterResponse>

    @POST("/login")
    fun loginUser(@Body request: LoginRequest): Call<RegisterResponse>

    @PUT("/user/goal")
    fun updateGoal(
        @Header("Authorization") token: String,
        @Body request: GoalRequest
    ): Call<GoalResponse>

    @POST("/forgot-password")
    fun forgotPassword(@Body request: ForgotPasswordRequest): Call<CommonResponse>

    @POST("/verify-otp")
    fun verifyOtp(@Body request: VerifyOtpRequest): Call<CommonResponse>

    @POST("/resend-otp")
    fun resendOtp(@Body request: ForgotPasswordRequest): Call<CommonResponse>

    @POST("/reset-password")
    fun resetPassword(@Body request: ResetPasswordRequest): Call<CommonResponse>

    @PUT("/user/password")
    fun updatePassword(
        @Header("Authorization") token: String,
        @Body request: ChangePasswordRequest
    ): Call<CommonResponse>

    @PUT("/user/name")
    fun updateName(
        @Header("Authorization") token: String,
        @Body request: NameRequest
    ): Call<CommonResponse>

    @Multipart
    @POST("/user/photo")
    fun uploadPhoto(
        @Header("Authorization") token: String,
        @Part photo: MultipartBody.Part
    ): Call<CommonResponse>

    @DELETE("/user/account")
    fun deleteAccount(
        @Header("Authorization") token: String
    ): Call<CommonResponse>

    @GET("/subjects")
    fun getSubjects(
        @Header("Authorization") token: String
    ): Call<List<Subject>>

    @POST("/subjects")
    fun addSubject(
        @Header("Authorization") token: String,
        @Body request: SubjectRequest
    ): Call<CommonResponse>

    @PUT("/subjects/{subject_id}")
    fun updateSubject(
        @Header("Authorization") token: String,
        @Path("subject_id") subjectId: Int,
        @Body request: NameRequest
    ): Call<CommonResponse>

    @DELETE("/subjects/{subject_id}")
    fun deleteSubject(
        @Header("Authorization") token: String,
        @Path("subject_id") subjectId: Int
    ): Call<CommonResponse>

    @POST("/attendance")
    fun recordAttendance(
        @Header("Authorization") token: String,
        @Body request: AttendanceRequest
    ): Call<CommonResponse>
}
