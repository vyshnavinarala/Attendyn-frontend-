package com.simats.attendyn

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        startAnimations()

        // Delay for 3 seconds and then launch IntroActivity
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, IntroActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000)
    }

    private fun startAnimations() {
        val dot1 = findViewById<View>(R.id.dot1)
        val dot2 = findViewById<View>(R.id.dot2)
        val dot3 = findViewById<View>(R.id.dot3)
        val sparkles = findViewById<ImageView>(R.id.logo_sparkles)
        val appName = findViewById<TextView>(R.id.app_name_text)
        val tagline = findViewById<TextView>(R.id.tagline_text)

        // 1. Loading Dots Animation (Wave effect)
        val dots = listOf(dot1, dot2, dot3)
        dots.forEachIndexed { index, dot ->
            val anim = ObjectAnimator.ofFloat(dot, "translationY", 0f, -15f, 0f)
            anim.duration = 600
            anim.startDelay = (index * 150).toLong()
            anim.repeatCount = ValueAnimator.INFINITE
            anim.repeatMode = ValueAnimator.REVERSE
            anim.start()
            
            // Sync alpha with movement
            val alphaAnim = ObjectAnimator.ofFloat(dot, "alpha", 0.5f, 1f, 0.5f)
            alphaAnim.duration = 600
            alphaAnim.startDelay = (index * 150).toLong()
            alphaAnim.repeatCount = ValueAnimator.INFINITE
            alphaAnim.repeatMode = ValueAnimator.REVERSE
            alphaAnim.start()
        }

        // 2. Stars/Sparkles Movement (Subtle rotation and scale pulse)
        val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 0.9f, 1.1f)
        val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.9f, 1.1f)
        val rotation = PropertyValuesHolder.ofFloat(View.ROTATION, -5f, 5f)
        
        ObjectAnimator.ofPropertyValuesHolder(sparkles, scaleX, scaleY, rotation).apply {
            duration = 2000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            start()
        }

        // 3. Text Color Pulse (White to Light Purple/Background tint)
        // Background startColor is #B666FF
        val colorAnim = ValueAnimator.ofObject(ArgbEvaluator(), 0xFFFFFFFF.toInt(), 0xFFB666FF.toInt())
        colorAnim.duration = 1500
        colorAnim.repeatCount = ValueAnimator.INFINITE
        colorAnim.repeatMode = ValueAnimator.REVERSE
        colorAnim.addUpdateListener { animator ->
            val color = animator.animatedValue as Int
            appName.setTextColor(color)
            tagline.setTextColor(color)
        }
        colorAnim.start()
    }
}
