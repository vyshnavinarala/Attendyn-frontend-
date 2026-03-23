package com.simats.attendyn

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.simats.attendyn.model.LoginRequest
import com.simats.attendyn.model.RegisterResponse
import com.simats.attendyn.network.RetrofitClient
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val loginButton: AppCompatButton = findViewById(R.id.login_button)
        val forgotPasswordLink: TextView = findViewById(R.id.forgot_password_link)
        val registerLink: TextView = findViewById(R.id.register_link)
        val emailEditText: EditText = findViewById(R.id.email_edit_text)
        val passwordEditText: EditText = findViewById(R.id.password_edit_text)
        val passwordToggle: ImageView = findViewById(R.id.password_visibility_toggle)

        var isPasswordVisible = false

        passwordToggle.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                passwordEditText.transformationMethod = HideReturnsTransformationMethod.getInstance()
                passwordToggle.setImageResource(R.drawable.ic_eye)
            } else {
                passwordEditText.transformationMethod = PasswordTransformationMethod.getInstance()
                passwordToggle.setImageResource(R.drawable.ic_eye_off)
            }
            passwordEditText.setSelection(passwordEditText.text.length)
        }

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty()) {
                emailEditText.error = "Email is required"
                emailEditText.requestFocus()
                return@setOnClickListener
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailEditText.error = "Invalid email format"
                emailEditText.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                passwordEditText.error = "Password is required"
                passwordEditText.requestFocus()
                return@setOnClickListener
            }

            // Show progress
            loginButton.isEnabled = false
            loginButton.text = "Logging in..."

            val loginRequest = LoginRequest(email, password)
            
            RetrofitClient.instance.loginUser(loginRequest).enqueue(object : Callback<RegisterResponse> {
                override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                    loginButton.isEnabled = true
                    loginButton.text = "Login"

                    if (response.isSuccessful) {
                        val loginResponse = response.body()
                        Toast.makeText(this@LoginActivity, loginResponse?.message ?: "Login Successful", Toast.LENGTH_SHORT).show()
                        
                        // Save user info and token to SharedPreferences
                        val prefs = getSharedPreferences("AttendynPrefs", MODE_PRIVATE)
                        val existingPhotoPath = prefs.getString("USER_PHOTO_PATH", null)
                        val existingEmail = prefs.getString("USER_EMAIL", null)
                        
                        prefs.edit().apply {
                            clear()
                            putString("USER_NAME", loginResponse?.user?.name)
                            putString("USER_EMAIL", loginResponse?.user?.email)
                            putString("AUTH_TOKEN", loginResponse?.token)
                            putInt("USER_ID", loginResponse?.user?.id ?: -1)
                            putInt("ATTENDANCE_GOAL", loginResponse?.user?.attendanceGoal ?: 75)
                            
                            // Save photo path from server or restore local if available
                            val serverPhotoPath = loginResponse?.user?.photoPath
                            if (serverPhotoPath != null) {
                                putString("USER_PHOTO_PATH", serverPhotoPath)
                            } else if (existingPhotoPath != null && existingEmail == loginResponse?.user?.email) {
                                putString("USER_PHOTO_PATH", existingPhotoPath)
                            }
                            apply()
                        }
                        
                        // Navigate to MainActivity
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        val errorMsg = try {
                            val errorBody = response.errorBody()?.string()
                            val errorResponse = Gson().fromJson(errorBody, RegisterResponse::class.java)
                            errorResponse.error ?: "Login failed"
                        } catch (e: Exception) {
                            "Login failed: ${response.code()}"
                        }
                        Toast.makeText(this@LoginActivity, errorMsg, Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                    loginButton.isEnabled = true
                    loginButton.text = "Login"
                    Toast.makeText(this@LoginActivity, "Network Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }

        forgotPasswordLink.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        registerLink.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}
