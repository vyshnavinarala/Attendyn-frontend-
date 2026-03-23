package com.simats.attendyn

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import android.widget.Toast
import com.simats.attendyn.model.ResetPasswordRequest
import com.simats.attendyn.model.CommonResponse
import com.simats.attendyn.network.RetrofitClient
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ResetPasswordActivity : AppCompatActivity() {

    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        val backButton: ImageButton = findViewById(R.id.back_button)
        val newPasswordEditText: EditText = findViewById(R.id.new_password_edit_text)
        val confirmPasswordEditText: EditText = findViewById(R.id.confirm_password_edit_text)
        val eyeIcon: ImageView = findViewById(R.id.new_password_eye)
        val resetPasswordButton: AppCompatButton = findViewById(R.id.reset_password_button)

        backButton.setOnClickListener {
            onBackPressed()
        }

        eyeIcon.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                newPasswordEditText.transformationMethod = HideReturnsTransformationMethod.getInstance()
                eyeIcon.setImageResource(R.drawable.ic_eye) // You might want a slashed eye icon for "hide"
            } else {
                newPasswordEditText.transformationMethod = PasswordTransformationMethod.getInstance()
                eyeIcon.setImageResource(R.drawable.ic_eye)
            }
            newPasswordEditText.setSelection(newPasswordEditText.text.length)
        }

        val email = intent.getStringExtra("EMAIL") ?: ""

        resetPasswordButton.setOnClickListener {
            val newPassword = newPasswordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                if (newPassword.isEmpty()) newPasswordEditText.error = "Enter new password"
                if (confirmPassword.isEmpty()) confirmPasswordEditText.error = "Confirm your password"
            } else if (newPassword.length < 6) {
                newPasswordEditText.error = "Password must be at least 6 characters"
                newPasswordEditText.requestFocus()
            } else if (!newPassword.any { it.isUpperCase() }) {
                newPasswordEditText.error = "Must contain at least one uppercase letter"
                newPasswordEditText.requestFocus()
            } else if (!newPassword.any { it.isDigit() }) {
                newPasswordEditText.error = "Must contain at least one number"
                newPasswordEditText.requestFocus()
            } else if (!newPassword.any { it.isLetterOrDigit().not() }) {
                newPasswordEditText.error = "Must contain at least one special character"
                newPasswordEditText.requestFocus()
            } else if (newPassword != confirmPassword) {
                confirmPasswordEditText.error = "Passwords do not match"
            } else {
                // Show progress
                resetPasswordButton.isEnabled = false
                resetPasswordButton.text = "Resetting..."

                val request = ResetPasswordRequest(email, newPassword)
                RetrofitClient.instance.resetPassword(request).enqueue(object : Callback<CommonResponse> {
                    override fun onResponse(call: Call<CommonResponse>, response: Response<CommonResponse>) {
                        resetPasswordButton.isEnabled = true
                        resetPasswordButton.text = "Reset Password"

                        if (response.isSuccessful) {
                            Toast.makeText(this@ResetPasswordActivity, response.body()?.message ?: "Password Reset Successful", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@ResetPasswordActivity, ResetSuccessActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            val errorMsg = try {
                                val errorBody = response.errorBody()?.string()
                                val errorResponse = Gson().fromJson(errorBody, CommonResponse::class.java)
                                errorResponse.error ?: "Failed to reset password"
                            } catch (e: Exception) {
                                "Error: ${response.code()}"
                            }
                            Toast.makeText(this@ResetPasswordActivity, errorMsg, Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<CommonResponse>, t: Throwable) {
                        resetPasswordButton.isEnabled = true
                        resetPasswordButton.text = "Reset Password"
                        Toast.makeText(this@ResetPasswordActivity, "Network Error: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                })
            }
        }
    }
}
