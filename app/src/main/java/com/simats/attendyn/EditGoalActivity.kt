package com.simats.attendyn

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.attendyn.model.GoalRequest
import com.simats.attendyn.model.GoalResponse
import com.simats.attendyn.network.RetrofitClient
import com.google.android.material.button.MaterialButton

class EditGoalActivity : AppCompatActivity() {

    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var circularProgressBar: ProgressBar
    private lateinit var tvPercentage: TextView
    private lateinit var attendanceSeekBar: SeekBar
    private lateinit var btnSaveGoal: androidx.appcompat.widget.AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_goal)

        initViews()
        loadCurrentGoal()
        setupListeners()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        circularProgressBar = findViewById(R.id.circular_progress_bar)
        tvPercentage = findViewById(R.id.tv_percentage)
        attendanceSeekBar = findViewById(R.id.attendance_seekbar)
        btnSaveGoal = findViewById(R.id.btn_save_goal)
    }

    private fun loadCurrentGoal() {
        val prefs = getSharedPreferences("AttendynPrefs", MODE_PRIVATE)
        val currentGoal = prefs.getInt("ATTENDANCE_GOAL", 75).coerceIn(50, 100)
        
        attendanceSeekBar.progress = currentGoal
        circularProgressBar.progress = currentGoal
        tvPercentage.text = "$currentGoal%"
    }

    private fun setupListeners() {
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        attendanceSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val clampedProgress = if (progress < 50) 50 else progress
                if (fromUser && progress < 50) {
                    seekBar?.progress = 50
                }
                tvPercentage.text = "$clampedProgress%"
                circularProgressBar.setProgress(clampedProgress, true)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        btnSaveGoal.setOnTouchListener { view, event ->
            when (event.action) {
                android.view.MotionEvent.ACTION_DOWN -> {
                    view.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).start()
                }
                android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                    view.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
                }
            }
            false
        }

        btnSaveGoal.setOnClickListener {
            saveGoal()
        }
    }

    private fun saveGoal() {
        val newGoal = attendanceSeekBar.progress.coerceIn(50, 100) // Enforce 50-100% goal
        val prefs = getSharedPreferences("AttendynPrefs", MODE_PRIVATE)
        val token = prefs.getString("AUTH_TOKEN", null)

        if (token == null) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
            return
        }

        btnSaveGoal.isEnabled = false
        val request = GoalRequest(newGoal)
        
        RetrofitClient.instance.updateGoal("Bearer $token", request)
            .enqueue(object : retrofit2.Callback<GoalResponse> {
                override fun onResponse(
                    call: retrofit2.Call<GoalResponse>,
                    response: retrofit2.Response<GoalResponse>
                ) {
                    btnSaveGoal.isEnabled = true
                    if (response.isSuccessful) {
                        prefs.edit().putInt("ATTENDANCE_GOAL", newGoal).apply()
                        Toast.makeText(this@EditGoalActivity, "Goal updated successfully!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        val errorMsg = response.errorBody()?.string() ?: "Update failed"
                        Toast.makeText(this@EditGoalActivity, "Error: $errorMsg", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<GoalResponse>, t: Throwable) {
                    btnSaveGoal.isEnabled = true
                    Toast.makeText(this@EditGoalActivity, "Network Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }
}
