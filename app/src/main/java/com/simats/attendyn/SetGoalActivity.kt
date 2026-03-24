package com.simats.attendyn

import android.content.Intent
import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.simats.attendyn.model.GoalRequest
import com.simats.attendyn.model.GoalResponse
import com.simats.attendyn.network.RetrofitClient
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SetGoalActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_goal)

        val percentageText: TextView = findViewById(R.id.percentage_text)
        val seekBar: SeekBar = findViewById(R.id.attendance_seekbar)
        val progressBar: android.widget.ProgressBar = findViewById(R.id.circular_progress_bar)
        val continueButton: AppCompatButton = findViewById(R.id.continue_button)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                percentageText.text = "$progress%"
                progressBar.setProgress(progress, true)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        continueButton.setOnClickListener {
            val goal = seekBar.progress.coerceIn(50, 100) // Enforce 50-100% goal
            
            // Show progress
            continueButton.isEnabled = false
            continueButton.text = "Saving..."

            val prefs = getSharedPreferences("AttendynPrefs", MODE_PRIVATE)
            val token = prefs.getString("AUTH_TOKEN", "") ?: ""
            
            val goalRequest = GoalRequest(goal)
            
            RetrofitClient.instance.updateGoal("Bearer $token", goalRequest).enqueue(object : Callback<GoalResponse> {
                override fun onResponse(call: Call<GoalResponse>, response: Response<GoalResponse>) {
                    continueButton.isEnabled = true
                    continueButton.text = "Continue"

                    if (response.isSuccessful) {
                        // Clear previous subjects for new registration
                        prefs.edit().remove("SUBJECTS_JSON").apply()
                        
                        // Save goal to SharedPreferences locally too if needed
                        prefs.edit().putInt("ATTENDANCE_GOAL", goal).apply()

                        // Navigate to AddSubjectsActivity
                        val intent = Intent(this@SetGoalActivity, AddSubjectsActivity::class.java)
                        intent.putExtra("ATTENDANCE_GOAL", goal)
                        intent.putExtra("EXTRA_IS_ONBOARDING", true)
                        startActivity(intent)
                        finish()
                    } else {
                        val errorMsg = try {
                            val errorBody = response.errorBody()?.string()
                            val errorResponse = Gson().fromJson(errorBody, GoalResponse::class.java)
                            errorResponse.error ?: "Failed to update goal"
                        } catch (e: Exception) {
                            "Error: ${response.code()}"
                        }
                        Toast.makeText(this@SetGoalActivity, errorMsg, Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<GoalResponse>, t: Throwable) {
                    continueButton.isEnabled = true
                    continueButton.text = "Continue"
                    Toast.makeText(this@SetGoalActivity, "Network Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }
    }
}
