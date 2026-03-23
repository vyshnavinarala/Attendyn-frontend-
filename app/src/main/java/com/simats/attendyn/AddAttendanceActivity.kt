package com.simats.attendyn

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.simats.attendyn.model.AttendanceRequest
import com.simats.attendyn.model.CommonResponse
import com.simats.attendyn.model.Subject
import com.simats.attendyn.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddAttendanceActivity : AppCompatActivity() {

    private lateinit var subjectSpinner: Spinner
    private lateinit var btnPresent: LinearLayout
    private lateinit var btnAbsent: LinearLayout
    private lateinit var ivPresent: ImageView
    private lateinit var tvPresent: TextView
    private lateinit var ivAbsent: ImageView
    private lateinit var tvAbsent: TextView
    private lateinit var btnSave: AppCompatButton
    private lateinit var emptyStateContainer: View
    private lateinit var normalStateGroup: androidx.constraintlayout.widget.Group

    private var selectedStatus: String? = null // "Present" or "Absent"
    private val subjectsList = ArrayList<Subject>()
    private val subjectNames = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_attendance)

        initViews()
        loadSubjects()
        setupListeners()
    }

    private fun loadSubjects() {
        val prefs = getSharedPreferences("AttendynPrefs", MODE_PRIVATE)
        val json = prefs.getString("SUBJECTS_JSON", null)
        if (json != null) {
            val gson = Gson()
            val type = object : TypeToken<ArrayList<Subject>>() {}.type
            val savedSubjects: ArrayList<Subject> = gson.fromJson(json, type)
            subjectsList.clear()
            subjectsList.addAll(savedSubjects)
            
            subjectNames.clear()
            subjectNames.add("Choose subject")
            for (subject in subjectsList) {
                subjectNames.add(subject.name)
            }
        }

        // Update Spinner Adapter
        (subjectSpinner.adapter as? ArrayAdapter<String>)?.notifyDataSetChanged()

        // Toggle Empty State Visibility
        if (subjectsList.isEmpty()) {
            emptyStateContainer.visibility = View.VISIBLE
            normalStateGroup.visibility = View.GONE
        } else {
            emptyStateContainer.visibility = View.GONE
            normalStateGroup.visibility = View.VISIBLE
            
            if (subjectNames.size == 0) {
                 subjectNames.add("No subjects added")
            }
        }
    }

    private fun initViews() {
        subjectSpinner = findViewById(R.id.subject_spinner)
        btnPresent = findViewById(R.id.btn_present)
        btnAbsent = findViewById(R.id.btn_absent)
        ivPresent = findViewById(R.id.iv_present)
        tvPresent = findViewById(R.id.tv_present)
        ivAbsent = findViewById(R.id.iv_absent)
        tvAbsent = findViewById(R.id.tv_absent)
        btnSave = findViewById(R.id.btn_save_attendance)
        emptyStateContainer = findViewById(R.id.empty_state_container)
        normalStateGroup = findViewById(R.id.normal_state_group)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, subjectNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        subjectSpinner.adapter = adapter

        btnSave.isEnabled = false
        btnSave.alpha = 0.5f

        // Bottom Nav Initialization and Listener
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_add
        bottomNav.labelVisibilityMode = com.google.android.material.navigation.NavigationBarView.LABEL_VISIBILITY_LABELED
        
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    // Navigate back to MainActivity (Home)
                    val intent = android.content.Intent(this, MainActivity::class.java)
                    intent.flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_add -> true
                R.id.nav_calendar -> {
                    startActivity(android.content.Intent(this, CalendarActivity::class.java))
                    true
                }
                R.id.nav_analysis -> {
                    startActivity(android.content.Intent(this, AnalysisActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(android.content.Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }

        findViewById<View>(R.id.fab_add).setOnClickListener {
            // Already on Add screen, just clear selection or toast
            Toast.makeText(this, "You're already on the add attendance screen", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupListeners() {
        btnPresent.setOnClickListener {
            selectStatus("Present")
        }

        btnAbsent.setOnClickListener {
            selectStatus("Absent")
        }

        btnSave.setOnClickListener {
            saveAttendance()
        }

        // Listener for subject selection to enable/disable button
        subjectSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                checkValidation()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun selectStatus(status: String) {
        selectedStatus = status
        
        if (status == "Present") {
            btnPresent.isSelected = true
            btnAbsent.isSelected = false
            
            updateButtonStyle(ivPresent, tvPresent, R.color.attendance_green, true)
            updateButtonStyle(ivAbsent, tvAbsent, null, false)
        } else {
            btnPresent.isSelected = false
            btnAbsent.isSelected = true
            
            updateButtonStyle(ivPresent, tvPresent, null, false)
            updateButtonStyle(ivAbsent, tvAbsent, R.color.attendance_red, true)
        }
        
        checkValidation()
    }

    private fun updateButtonStyle(iv: ImageView, tv: TextView, colorRes: Int?, isSelected: Boolean) {
        if (isSelected && colorRes != null) {
            val color = ContextCompat.getColor(this, colorRes)
            iv.imageTintList = ColorStateList.valueOf(color)
            tv.setTextColor(color)
        } else {
            val neutralColor = ContextCompat.getColor(this, R.color.forgot_password_bg_end) // Using existing grey
            iv.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.darker_gray))
            tv.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))
        }
    }

    private fun checkValidation() {
        val isSubjectSelected = subjectSpinner.selectedItemPosition > 0
        val isStatusSelected = selectedStatus != null
        
        btnSave.isEnabled = isSubjectSelected && isStatusSelected
        btnSave.alpha = if (btnSave.isEnabled) 1.0f else 0.5f
    }

    private fun saveAttendance() {
        val selectedSubjectName = subjectSpinner.selectedItem as String
        val subject = subjectsList.find { it.name == selectedSubjectName } ?: return
        val subjectId = subject.id ?: return

        val statusInt = if (selectedStatus == "Present") 1 else 2
        
        val prefs = getSharedPreferences("AttendynPrefs", MODE_PRIVATE)
        val token = prefs.getString("AUTH_TOKEN", null)

        if (token == null) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
            return
        }

        btnSave.isEnabled = false
        btnSave.alpha = 0.5f

        val request = AttendanceRequest(subjectId, statusInt)
        RetrofitClient.instance.recordAttendance("Bearer $token", request)
            .enqueue(object : Callback<CommonResponse> {
                override fun onResponse(call: Call<CommonResponse>, response: Response<CommonResponse>) {
                    btnSave.isEnabled = true
                    btnSave.alpha = 1.0f

                    if (response.isSuccessful) {
                        Toast.makeText(this@AddAttendanceActivity, response.body()?.message ?: "Attendance recorded successfully", Toast.LENGTH_SHORT).show()
                        
                        // Navigate back to dashboard
                        val intent = android.content.Intent(this@AddAttendanceActivity, MainActivity::class.java)
                        intent.flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
                        startActivity(intent)
                        finish()
                    } else {
                        val errorMsg = response.errorBody()?.string() ?: "Failed to record attendance"
                        Toast.makeText(this@AddAttendanceActivity, "Error: $errorMsg", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<CommonResponse>, t: Throwable) {
                    btnSave.isEnabled = true
                    btnSave.alpha = 1.0f
                    Toast.makeText(this@AddAttendanceActivity, "Network Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }
}
