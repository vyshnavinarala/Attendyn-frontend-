package com.simats.attendyn

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.simats.attendyn.databinding.ActivityAccountSuccessBinding

class AccountSuccessActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAccountSuccessBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountSuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.continueButton.setOnClickListener {
            // Navigate to SetGoalActivity
            val intent = Intent(this, SetGoalActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
