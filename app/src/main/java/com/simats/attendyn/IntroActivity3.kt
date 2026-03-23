package com.simats.attendyn

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class IntroActivity3 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro3)

        val getStartedButton: AppCompatButton = findViewById(R.id.get_started_button)
        val backButton: ImageButton = findViewById(R.id.back_button)

        getStartedButton.setOnClickListener {
            // Navigate to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        backButton.setOnClickListener {
            // Navigate back to IntroActivity2
            val intent = Intent(this, IntroActivity2::class.java)
            startActivity(intent)
            finish()
        }
    }
}
