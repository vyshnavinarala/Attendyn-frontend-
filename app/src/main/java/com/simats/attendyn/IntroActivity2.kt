package com.simats.attendyn

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class IntroActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro2)

        val nextButton: AppCompatButton = findViewById(R.id.next_button)
        val skipButton: TextView = findViewById(R.id.skip_button)
        val backButton: ImageButton = findViewById(R.id.back_button)

        nextButton.setOnClickListener {
            // Navigate to IntroActivity3
            val intent = Intent(this, IntroActivity3::class.java)
            startActivity(intent)
            finish()
        }

        skipButton.setOnClickListener {
            // Navigate to IntroActivity3
            val intent = Intent(this, IntroActivity3::class.java)
            startActivity(intent)
            finish()
        }

        backButton.setOnClickListener {
            // Navigate back to IntroActivity
            val intent = Intent(this, IntroActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
