package com.simats.attendyn

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import android.widget.Toast
import com.simats.attendyn.model.VerifyOtpRequest
import com.simats.attendyn.model.ForgotPasswordRequest
import com.simats.attendyn.model.CommonResponse
import com.simats.attendyn.network.RetrofitClient
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OtpVerificationActivity : AppCompatActivity() {

    private lateinit var otpFields: Array<EditText>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp_verification)

        val backButton: ImageButton = findViewById(R.id.back_button)
        val verifyButton: AppCompatButton = findViewById(R.id.verify_button)
        val resendText: TextView = findViewById(R.id.resend_text)

        otpFields = arrayOf(
            findViewById(R.id.otp_1),
            findViewById(R.id.otp_2),
            findViewById(R.id.otp_3),
            findViewById(R.id.otp_4),
            findViewById(R.id.otp_5),
            findViewById(R.id.otp_6)
        )

        setupOtpFields()

        backButton.setOnClickListener {
            onBackPressed()
        }

        val email = intent.getStringExtra("EMAIL") ?: ""
        val subtitleText: TextView = findViewById(R.id.subtitle_verify_otp)
        subtitleText.text = "Enter the 6-digit code sent to\n$email"

        verifyButton.setOnClickListener {
            val otp = otpFields.joinToString("") { it.text.toString() }
            if (otp.length == 6) {
                // Show progress
                verifyButton.isEnabled = false
                verifyButton.text = "Verifying..."

                val request = VerifyOtpRequest(email, otp)
                RetrofitClient.instance.verifyOtp(request).enqueue(object : Callback<CommonResponse> {
                    override fun onResponse(call: Call<CommonResponse>, response: Response<CommonResponse>) {
                        verifyButton.isEnabled = true
                        verifyButton.text = "Verify"

                        if (response.isSuccessful) {
                            Toast.makeText(this@OtpVerificationActivity, response.body()?.message ?: "OTP Verified", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@OtpVerificationActivity, ResetPasswordActivity::class.java)
                            intent.putExtra("EMAIL", email)
                            startActivity(intent)
                            finish()
                        } else {
                            val errorMsg = try {
                                val errorBody = response.errorBody()?.string()
                                val errorResponse = Gson().fromJson(errorBody, CommonResponse::class.java)
                                errorResponse.error ?: "Invalid OTP"
                            } catch (e: Exception) {
                                "Error: ${response.code()}"
                            }
                            Toast.makeText(this@OtpVerificationActivity, errorMsg, Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<CommonResponse>, t: Throwable) {
                        verifyButton.isEnabled = true
                        verifyButton.text = "Verify"
                        Toast.makeText(this@OtpVerificationActivity, "Network Error: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                })
            } else {
                Toast.makeText(this, "Please enter 6-digit OTP", Toast.LENGTH_SHORT).show()
            }
        }

        resendText.setOnClickListener {
            // Handle actual resend logic here using forgotPassword endpoint
            val request = ForgotPasswordRequest(email)
            resendText.text = "Resending..."
            RetrofitClient.instance.resendOtp(request).enqueue(object : Callback<CommonResponse> {
                override fun onResponse(call: Call<CommonResponse>, response: Response<CommonResponse>) {
                    resendText.text = "Resend"
                    if (response.isSuccessful) {
                        Toast.makeText(this@OtpVerificationActivity, "OTP Resent", Toast.LENGTH_SHORT).show()
                        startResendTimer()
                    } else {
                        Toast.makeText(this@OtpVerificationActivity, "Failed to resend OTP", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<CommonResponse>, t: Throwable) {
                    resendText.text = "Resend"
                    Toast.makeText(this@OtpVerificationActivity, "Network Error", Toast.LENGTH_SHORT).show()
                }
            })
        }

        startResendTimer()
    }

    private fun startResendTimer() {
        val resendText: TextView = findViewById(R.id.resend_text)
        val timerText: TextView = findViewById(R.id.timer_text)

        resendText.isEnabled = false
        resendText.alpha = 0.5f
        timerText.visibility = android.view.View.VISIBLE

        object : android.os.CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timerText.text = " in ${millisUntilFinished / 1000}s"
            }

            override fun onFinish() {
                resendText.isEnabled = true
                resendText.alpha = 1.0f
                timerText.visibility = android.view.View.GONE
            }
        }.start()
    }

    private fun setupOtpFields() {
        for (i in otpFields.indices) {
            otpFields[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1 && i < otpFields.size - 1) {
                        otpFields[i + 1].requestFocus()
                    }
                }
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 0 && i > 0) {
                        // This case is handled by onKeyListener for better UX
                    }
                }
            })

            otpFields[i].setOnKeyListener { v, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                    if (otpFields[i].text.isEmpty() && i > 0) {
                        otpFields[i - 1].requestFocus()
                        otpFields[i - 1].text?.clear()
                        return@setOnKeyListener true
                    }
                }
                false
            }
        }
    }
}
