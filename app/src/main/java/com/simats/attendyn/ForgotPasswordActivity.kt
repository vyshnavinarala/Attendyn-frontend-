package com.simats.attendyn

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import android.widget.Toast
import com.simats.attendyn.model.ForgotPasswordRequest
import com.simats.attendyn.model.CommonResponse
import com.simats.attendyn.network.RetrofitClient
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForgotPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        val backButton: ImageButton = findViewById(R.id.back_button)
        val emailEditText: EditText = findViewById(R.id.email_edit_text)
        val sendOtpButton: AppCompatButton = findViewById(R.id.send_otp_button)
        val loginText: android.widget.TextView = findViewById(R.id.login_text)

        backButton.setOnClickListener {
            onBackPressed()
        }

        sendOtpButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            if (email.isNotEmpty()) {
                // Show progress
                sendOtpButton.isEnabled = false
                sendOtpButton.text = "Sending..."

                val request = ForgotPasswordRequest(email)
                RetrofitClient.instance.forgotPassword(request).enqueue(object : Callback<CommonResponse> {
                    override fun onResponse(call: Call<CommonResponse>, response: Response<CommonResponse>) {
                        sendOtpButton.isEnabled = true
                        sendOtpButton.text = "Send OTP"

                        if (response.isSuccessful) {
                            Toast.makeText(this@ForgotPasswordActivity, response.body()?.message ?: "OTP Sent", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@ForgotPasswordActivity, OtpVerificationActivity::class.java)
                            intent.putExtra("EMAIL", email)
                            startActivity(intent)
                        } else {
                            val errorMsg = try {
                                val errorBody = response.errorBody()?.string()
                                val errorResponse = Gson().fromJson(errorBody, CommonResponse::class.java)
                                errorResponse.error ?: "Failed to send OTP"
                            } catch (e: Exception) {
                                "Error: ${response.code()}"
                            }
                            Toast.makeText(this@ForgotPasswordActivity, errorMsg, Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<CommonResponse>, t: Throwable) {
                        sendOtpButton.isEnabled = true
                        sendOtpButton.text = "Send OTP"
                        Toast.makeText(this@ForgotPasswordActivity, "Network Error: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                })
            } else {
                emailEditText.error = "Please enter your email"
            }
        }

        loginText.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }
}
