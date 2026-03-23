package com.simats.attendyn

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.simats.attendyn.databinding.ActivityResetSuccessBinding

class ResetSuccessActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResetSuccessBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetSuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backToLoginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
