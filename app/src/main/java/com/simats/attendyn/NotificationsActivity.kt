package com.simats.attendyn

import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.materialswitch.MaterialSwitch

class NotificationsActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var switchDailyReminder: MaterialSwitch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        initViews()
        loadSwitchState()
        setupListeners()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btn_back)
        switchDailyReminder = findViewById(R.id.switch_daily_reminder)
    }

    private fun loadSwitchState() {
        val prefs = getSharedPreferences("AttendynPrefs", MODE_PRIVATE)
        val isEnabled = prefs.getBoolean("DAILY_REMINDER_ENABLED", true)
        switchDailyReminder.isChecked = isEnabled
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        switchDailyReminder.setOnCheckedChangeListener { _, isChecked ->
            saveSwitchState(isChecked)
            val message = if (isChecked) "Daily reminders enabled" else "Daily reminders disabled"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveSwitchState(isEnabled: Boolean) {
        val prefs = getSharedPreferences("AttendynPrefs", MODE_PRIVATE)
        prefs.edit().putBoolean("DAILY_REMINDER_ENABLED", isEnabled).apply()
        
        // In a real app, you would also schedule/cancel the notification alarm here
    }
}
