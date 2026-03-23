package com.simats.attendyn

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.attendyn.model.ChangePasswordRequest
import com.simats.attendyn.model.CommonResponse
import com.simats.attendyn.network.RetrofitClient
import com.google.android.material.button.MaterialButton

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var etCurrentPassword: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnUpdate: android.widget.Button
    private lateinit var btnCancel: MaterialButton
    private lateinit var btnBack: ImageButton
    
    private lateinit var ivEyeCurrent: ImageView
    private lateinit var ivEyeNew: ImageView
    private lateinit var ivEyeConfirm: ImageView

    private var isCurrentVisible = false
    private var isNewVisible = false
    private var isConfirmVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        etCurrentPassword = findViewById(R.id.et_current_password)
        etNewPassword = findViewById(R.id.et_new_password)
        etConfirmPassword = findViewById(R.id.et_confirm_password)
        btnUpdate = findViewById(R.id.btn_update_password)
        btnCancel = findViewById(R.id.btn_cancel)
        btnBack = findViewById(R.id.btn_back)
        
        ivEyeCurrent = findViewById(R.id.iv_eye_current)
        ivEyeNew = findViewById(R.id.iv_eye_new)
        ivEyeConfirm = findViewById(R.id.iv_eye_confirm)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnCancel.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnUpdate.setOnClickListener {
            handleUpdatePassword()
        }

        ivEyeCurrent.setOnClickListener {
            isCurrentVisible = !isCurrentVisible
            togglePasswordVisibility(etCurrentPassword, ivEyeCurrent, isCurrentVisible)
        }

        ivEyeNew.setOnClickListener {
            isNewVisible = !isNewVisible
            togglePasswordVisibility(etNewPassword, ivEyeNew, isNewVisible)
        }

        ivEyeConfirm.setOnClickListener {
            isConfirmVisible = !isConfirmVisible
            togglePasswordVisibility(etConfirmPassword, ivEyeConfirm, isConfirmVisible)
        }
    }

    private fun togglePasswordVisibility(editText: EditText, imageView: ImageView, isVisible: Boolean) {
        if (isVisible) {
            editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
            imageView.setImageResource(R.drawable.ic_eye)
        } else {
            editText.transformationMethod = PasswordTransformationMethod.getInstance()
            imageView.setImageResource(R.drawable.ic_eye_off)
        }
        // Move cursor to end
        editText.setSelection(editText.text.length)
    }

    private fun handleUpdatePassword() {
        val current = etCurrentPassword.text.toString().trim()
        val new = etNewPassword.text.toString().trim()
        val confirm = etConfirmPassword.text.toString().trim()

        if (current.isEmpty() || new.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (new != confirm) {
            Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        if (new.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return
        }

        if (!new.any { it.isUpperCase() }) {
            Toast.makeText(this, "Must contain at least one uppercase letter", Toast.LENGTH_SHORT).show()
            return
        }

        if (!new.any { it.isDigit() }) {
            Toast.makeText(this, "Must contain at least one number", Toast.LENGTH_SHORT).show()
            return
        }

        if (!new.any { it.isLetterOrDigit().not() }) {
            Toast.makeText(this, "Must contain at least one special character", Toast.LENGTH_SHORT).show()
            return
        }

        val prefs = getSharedPreferences("AttendynPrefs", MODE_PRIVATE)
        val token = prefs.getString("AUTH_TOKEN", null)

        if (token == null) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
            return
        }

        btnUpdate.isEnabled = false
        val request = ChangePasswordRequest(current, new)

        RetrofitClient.instance.updatePassword("Bearer $token", request)
            .enqueue(object : retrofit2.Callback<CommonResponse> {
                override fun onResponse(
                    call: retrofit2.Call<CommonResponse>,
                    response: retrofit2.Response<CommonResponse>
                ) {
                    btnUpdate.isEnabled = true
                    if (response.isSuccessful) {
                        Toast.makeText(this@ChangePasswordActivity, "Password updated successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        val errorMsg = response.errorBody()?.string() ?: "Update failed"
                        Toast.makeText(this@ChangePasswordActivity, "Error: $errorMsg", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<CommonResponse>, t: Throwable) {
                    btnUpdate.isEnabled = true
                    Toast.makeText(this@ChangePasswordActivity, "Network Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }
}
