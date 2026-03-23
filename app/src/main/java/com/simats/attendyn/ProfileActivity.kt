package com.simats.attendyn

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.simats.attendyn.model.CommonResponse
import com.simats.attendyn.network.RetrofitClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.graphics.BitmapFactory
import java.net.URL
import kotlin.concurrent.thread

class ProfileActivity : AppCompatActivity() {

    private lateinit var tvUsername: TextView
    private lateinit var ivAvatarProfile: ImageView
    private lateinit var bottomNav: BottomNavigationView

    private val editProfileLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            loadUserData()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        initViews()
        loadUserData()
        setupListeners()
        setupBottomNavigation()
        setupBackNavigation()
    }

    override fun onResume() {
        super.onResume()
        if (::bottomNav.isInitialized && bottomNav.selectedItemId != R.id.nav_profile) {
            bottomNav.selectedItemId = R.id.nav_profile
        }
    }

    private fun initViews() {
        tvUsername = findViewById(R.id.tv_username)
        ivAvatarProfile = findViewById(R.id.iv_avatar_profile)
        bottomNav = findViewById(R.id.bottom_navigation)
    }

    private fun loadUserData() {
        val prefs = getSharedPreferences("AttendynPrefs", MODE_PRIVATE)
        val username = prefs.getString("USER_NAME", "User")
        val photoPath = prefs.getString("USER_PHOTO_PATH", null)

        tvUsername.text = username

        if (photoPath != null) {
            if (photoPath.startsWith("uploads/")) {
                // Load from server
                val fullUrl = RetrofitClient.BASE_URL + photoPath
                ivAvatarProfile.setPadding(0, 0, 0, 0)
                ivAvatarProfile.scaleType = ImageView.ScaleType.CENTER_CROP
                ivAvatarProfile.imageTintList = null
                
                thread {
                    try {
                        val inputStream = URL(fullUrl).openStream()
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        runOnUiThread {
                            ivAvatarProfile.setImageBitmap(bitmap)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } else {
                // Load from local file
                val file = java.io.File(photoPath)
                if (file.exists()) {
                    ivAvatarProfile.setPadding(0, 0, 0, 0)
                    ivAvatarProfile.scaleType = ImageView.ScaleType.CENTER_CROP
                    ivAvatarProfile.setImageURI(android.net.Uri.fromFile(file))
                    ivAvatarProfile.imageTintList = null // Clear tint for actual photo
                }
            }
        } else {
            // Fallback: check if the default local file exists
            val fallbackFile = java.io.File(filesDir, "profile_image.jpg")
            if (fallbackFile.exists()) {
                ivAvatarProfile.setPadding(0, 0, 0, 0)
                ivAvatarProfile.scaleType = ImageView.ScaleType.CENTER_CROP
                ivAvatarProfile.setImageURI(android.net.Uri.fromFile(fallbackFile))
                ivAvatarProfile.imageTintList = null
                // Restore the path in prefs for next time
                prefs.edit().putString("USER_PHOTO_PATH", fallbackFile.absolutePath).apply()
            }
        }
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@ProfileActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                finish()
            }
        })
    }

    private fun setupListeners() {
        findViewById<android.view.View>(R.id.item_logout).setOnClickListener {
            logout()
        }

        findViewById<android.view.View>(R.id.item_delete_account).setOnClickListener {
            showDeleteAccountDialog()
        }

        findViewById<android.view.View>(R.id.avatar_container).setOnClickListener {
            // Profile photo viewed or do nothing as per user's request
        }

        // Placeholder listeners for items
        findViewById<android.view.View>(R.id.item_edit_profile).setOnClickListener {
            editProfileLauncher.launch(Intent(this, EditProfileActivity::class.java))
        }

        val items = listOf(
            R.id.item_manage_subjects
        )

        findViewById<android.view.View>(R.id.item_edit_goal).setOnClickListener {
            startActivity(Intent(this, EditGoalActivity::class.java))
        }

        findViewById<android.view.View>(R.id.item_privacy).setOnClickListener {
            startActivity(Intent(this, PrivacyPolicyActivity::class.java))
        }

        findViewById<android.view.View>(R.id.item_change_password).setOnClickListener {
            startActivity(Intent(this, ChangePasswordActivity::class.java))
        }

        findViewById<android.view.View>(R.id.item_about).setOnClickListener {
            startActivity(Intent(this, AboutAppActivity::class.java))
        }

        findViewById<android.view.View>(R.id.item_manage_subjects).setOnClickListener {
            startActivity(Intent(this, ManageSubjectsActivity::class.java))
        }

        for (id in items) {
            if (id != R.id.item_manage_subjects) {
                findViewById<android.view.View>(id).setOnClickListener {
                    Toast.makeText(this, "This feature will be implemented in the next update", Toast.LENGTH_SHORT).show()
                }
            }
        }

        findViewById<android.view.View>(R.id.fab_add).setOnClickListener {
            startActivity(Intent(this, AddAttendanceActivity::class.java))
        }
    }

    private fun showLogoutDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_logout, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this, R.style.CustomDialogTheme)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))

        dialogView.findViewById<android.view.View>(R.id.btn_close).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<android.view.View>(R.id.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<android.view.View>(R.id.btn_logout_confirm).setOnClickListener {
            dialog.dismiss()
            performLogout()
        }

        dialog.show()
    }

    private fun performLogout() {
        val prefs = getSharedPreferences("AttendynPrefs", MODE_PRIVATE)
        prefs.edit().clear().apply() 
        
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
        
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun logout() {
        showLogoutDialog()
    }

    private fun showDeleteAccountDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_delete_account, null)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this, R.style.CustomDialogTheme)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))

        dialogView.findViewById<android.view.View>(R.id.btn_close).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<android.view.View>(R.id.btn_cancel).setOnClickListener {
            dialog.dismiss()
        }

        dialogView.findViewById<android.view.View>(R.id.btn_delete_confirm).setOnClickListener {
            dialog.dismiss()
            performAccountDeletion()
        }

        dialog.show()
    }

    private fun performAccountDeletion() {
        val prefs = getSharedPreferences("AttendynPrefs", MODE_PRIVATE)
        val token = prefs.getString("AUTH_TOKEN", null)

        if (token == null) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
            return
        }

        RetrofitClient.instance.deleteAccount("Bearer $token")
            .enqueue(object : retrofit2.Callback<CommonResponse> {
                override fun onResponse(
                    call: retrofit2.Call<CommonResponse>,
                    response: retrofit2.Response<CommonResponse>
                ) {
                    if (response.isSuccessful) {
                        // Delete profile photo if exists
                        val photoPath = prefs.getString("USER_PHOTO_PATH", null)
                        if (photoPath != null) {
                            val file = java.io.File(photoPath)
                            if (file.exists()) {
                                file.delete()
                            }
                        }
                        
                        // Clear all data
                        prefs.edit().clear().apply()
                        
                        Toast.makeText(this@ProfileActivity, "Account deleted successfully", Toast.LENGTH_SHORT).show()
                        
                        // Navigate back to login
                        val intent = Intent(this@ProfileActivity, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        val errorMsg = response.errorBody()?.string() ?: "Deletion failed"
                        Toast.makeText(this@ProfileActivity, "Error: $errorMsg", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<CommonResponse>, t: Throwable) {
                    Toast.makeText(this@ProfileActivity, "Network Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun setupBottomNavigation() {
        bottomNav.selectedItemId = R.id.nav_profile
        bottomNav.labelVisibilityMode = com.google.android.material.navigation.NavigationBarView.LABEL_VISIBILITY_LABELED
        
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_calendar -> {
                    startActivity(Intent(this, CalendarActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_analysis -> {
                    startActivity(Intent(this, AnalysisActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_add -> {
                    startActivity(Intent(this, AddAttendanceActivity::class.java))
                    false
                }
                R.id.nav_profile -> true
                else -> false
            }
        }
    }


}
