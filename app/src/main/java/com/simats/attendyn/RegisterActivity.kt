package com.simats.attendyn

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.attendyn.databinding.ActivityRegisterBinding
import com.simats.attendyn.model.RegisterRequest
import com.simats.attendyn.model.RegisterResponse
import com.simats.attendyn.network.RetrofitClient
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Password Visibility Toggle
        binding.passwordVisibilityToggle.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                binding.passwordEditText.transformationMethod = HideReturnsTransformationMethod.getInstance()
                binding.passwordVisibilityToggle.setImageResource(R.drawable.ic_eye)
            } else {
                binding.passwordEditText.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.passwordVisibilityToggle.setImageResource(R.drawable.ic_eye_off)
            }
            binding.passwordEditText.setSelection(binding.passwordEditText.text.length)
        }

        // Confirm Password Visibility Toggle
        binding.confirmPasswordVisibilityToggle.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible
            if (isConfirmPasswordVisible) {
                binding.confirmPasswordEditText.transformationMethod = HideReturnsTransformationMethod.getInstance()
                binding.confirmPasswordVisibilityToggle.setImageResource(R.drawable.ic_eye)
            } else {
                binding.confirmPasswordEditText.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.confirmPasswordVisibilityToggle.setImageResource(R.drawable.ic_eye_off)
            }
            binding.confirmPasswordEditText.setSelection(binding.confirmPasswordEditText.text.length)
        }

        binding.registerButton.setOnClickListener {
            val name = binding.nameEditText.text.toString()
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            val confirmPassword = binding.confirmPasswordEditText.text.toString()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.emailEditText.error = "Invalid email format"
                binding.emailEditText.requestFocus()
            } else if (password.length < 6) {
                binding.passwordEditText.error = "Password must be at least 6 characters"
                binding.passwordEditText.requestFocus()
            } else if (!password.any { it.isUpperCase() }) {
                binding.passwordEditText.error = "Must contain at least one uppercase letter"
                binding.passwordEditText.requestFocus()
            } else if (!password.any { it.isDigit() }) {
                binding.passwordEditText.error = "Must contain at least one number"
                binding.passwordEditText.requestFocus()
            } else if (!password.any { it.isLetterOrDigit().not() }) {
                binding.passwordEditText.error = "Must contain at least one special character"
                binding.passwordEditText.requestFocus()
            } else if (password != confirmPassword) {
                binding.confirmPasswordEditText.error = "Passwords do not match"
                binding.confirmPasswordEditText.requestFocus()
            } else {
                // Show progress
                binding.registerButton.isEnabled = false
                binding.registerButton.text = "Registering..."

                val registerRequest = RegisterRequest(name, email, password)
                
                RetrofitClient.instance.registerUser(registerRequest).enqueue(object : Callback<RegisterResponse> {
                    override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                        binding.registerButton.isEnabled = true
                        binding.registerButton.text = "Register"

                        if (response.isSuccessful) {
                            val registerResponse = response.body()
                            Toast.makeText(this@RegisterActivity, registerResponse?.message ?: "Registration Successful", Toast.LENGTH_SHORT).show()
                            
                            // Save user info and token to SharedPreferences
                            val prefs = getSharedPreferences("AttendynPrefs", MODE_PRIVATE)
                            prefs.edit().apply {
                                clear()
                                putString("USER_NAME", name)
                                putString("USER_EMAIL", email)
                                putString("AUTH_TOKEN", registerResponse?.token)
                                putInt("USER_ID", registerResponse?.user?.id ?: -1)
                                apply()
                            }
                            
                            // Navigate to AccountSuccessActivity
                            val intent = Intent(this@RegisterActivity, AccountSuccessActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            val errorMsg = try {
                                val errorBody = response.errorBody()?.string()
                                val errorResponse = Gson().fromJson(errorBody, RegisterResponse::class.java)
                                errorResponse.error ?: "Registration failed"
                            } catch (e: Exception) {
                                "Registration failed: ${response.code()}"
                            }
                            Toast.makeText(this@RegisterActivity, errorMsg, Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                        binding.registerButton.isEnabled = true
                        binding.registerButton.text = "Register"
                        Toast.makeText(this@RegisterActivity, "Network Error: ${t.message}", Toast.LENGTH_LONG).show()
                    }
                })
            }
        }

        binding.loginLink.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
