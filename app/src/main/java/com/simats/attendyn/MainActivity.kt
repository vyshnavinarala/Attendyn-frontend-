package com.simats.attendyn

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.simats.attendyn.model.Subject
import com.simats.attendyn.network.RetrofitClient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var welcomeText: TextView
    private lateinit var overallPercentageText: TextView
    private lateinit var overallRiskBadge: TextView
    private lateinit var overallGoalText: TextView
    private lateinit var overallProgressFront: ProgressBar
    private lateinit var insightText: TextView
    private lateinit var dashboardRecyclerView: RecyclerView
    private lateinit var emptyStateContainer: View
    private lateinit var btnEmptyAddSubject: View
    private lateinit var normalStateGroup: androidx.constraintlayout.widget.Group

    private var userGoal: Int = 75 // Default goal
    private var username: String = "Guest"
    private val subjectsList = ArrayList<Subject>()
    private var isShowingAllSubjects = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadPersistentData()
        initViews()
        updateDashboardUI()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        val bottomNav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
        if (bottomNav.selectedItemId != R.id.nav_dashboard) {
            bottomNav.selectedItemId = R.id.nav_dashboard
        }
        loadPersistentData()
        updateDashboardUI()
    }

    private fun loadPersistentData() {
        val prefs = getSharedPreferences("AttendynPrefs", MODE_PRIVATE)
        username = prefs.getString("USER_NAME", "User") ?: "User"
        userGoal = prefs.getInt("ATTENDANCE_GOAL", 75)
        
        val token = prefs.getString("AUTH_TOKEN", null)
        if (token != null) {
            fetchSubjectsFromServer(token)
        } else {
            // Offline fallback
            val json = prefs.getString("SUBJECTS_JSON", null)
            if (json != null) {
                val gson = Gson()
                val type = object : TypeToken<ArrayList<Subject>>() {}.type
                val savedSubjects: ArrayList<Subject> = gson.fromJson(json, type)
                subjectsList.clear()
                subjectsList.addAll(savedSubjects)
                updateDashboardUI()
            }
        }
    }

    private fun fetchSubjectsFromServer(token: String) {
        RetrofitClient.instance.getSubjects("Bearer $token")
            .enqueue(object : Callback<List<Subject>> {
                override fun onResponse(call: Call<List<Subject>>, response: Response<List<Subject>>) {
                    if (response.isSuccessful) {
                        response.body()?.let { savedSubjects ->
                            subjectsList.clear()
                            subjectsList.addAll(savedSubjects)
                            updateDashboardUI()
                            
                            // Cache locally
                            val json = Gson().toJson(savedSubjects)
                            getSharedPreferences("AttendynPrefs", MODE_PRIVATE)
                                .edit().putString("SUBJECTS_JSON", json).apply()
                        }
                    }
                }

                override fun onFailure(call: Call<List<Subject>>, t: Throwable) {
                    // Stay with cached data if network fails
                }
            })
    }

    private fun initViews() {
        welcomeText = findViewById(R.id.welcome_text)
        overallPercentageText = findViewById(R.id.overall_percentage_text)
        overallRiskBadge = findViewById(R.id.overall_risk_badge)
        overallGoalText = findViewById(R.id.overall_goal_text)
        overallProgressFront = findViewById(R.id.overall_progress_front)
        insightText = findViewById(R.id.insight_text)
        dashboardRecyclerView = findViewById(R.id.dashboard_recycler_view)
        emptyStateContainer = findViewById(R.id.empty_state_container)
        btnEmptyAddSubject = findViewById(R.id.btn_empty_add_subject)
        normalStateGroup = findViewById(R.id.normal_state_group)

        welcomeText.text = "${getGreeting()}, $username!"
        overallGoalText.text = "Goal: $userGoal%"
    }

    private fun getGreeting(): String {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 0..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
    }


    private fun updateDashboardUI() {
        var totalConducted = 0
        var totalAttended = 0
        var highRiskCount = 0

        for (subject in subjectsList) {
            totalConducted += subject.conducted
            totalAttended += subject.attended
            if (subject.percentage < (userGoal - 10)) {
                highRiskCount++
            }
        }

        val overallPercentage = if (totalConducted > 0) (totalAttended.toDouble() / totalConducted.toDouble()) * 100.0 else 0.0
        
        // Update Percentage Text and Progress
        overallPercentageText.text = String.format(Locale.US, "%.1f%%", overallPercentage)
        overallProgressFront.progress = overallPercentage.toInt()

        // Risk Logic
        val riskColor: Int
        val riskLabel: String
        val badgeBg: Int

        when {
            overallPercentage >= userGoal -> {
                riskColor = ContextCompat.getColor(this, R.color.attendance_green)
                riskLabel = "Safe"
                badgeBg = R.drawable.bg_badge_green
            }
            overallPercentage >= (userGoal - 10) -> {
                riskColor = ContextCompat.getColor(this, R.color.attendance_orange)
                riskLabel = "Medium"
                badgeBg = R.drawable.bg_badge_orange
            }
            else -> {
                riskColor = ContextCompat.getColor(this, R.color.attendance_red)
                riskLabel = "High"
                badgeBg = R.drawable.bg_badge_red
            }
        }

        overallRiskBadge.text = riskLabel
        overallRiskBadge.setTextColor(ContextCompat.getColor(this, R.color.white))
        overallRiskBadge.setBackgroundResource(badgeBg)
        overallProgressFront.progressTintList = ColorStateList.valueOf(riskColor)

        // Insight Logic
        if (highRiskCount > 0) {
            insightText.text = "You have $highRiskCount subjects at high risk. Attend upcoming classes to improve."
        } else {
            insightText.text = "Great! Your attendance is on track."
        }

        // Toggle Empty State Visibility
        if (subjectsList.isEmpty()) {
            emptyStateContainer.visibility = View.VISIBLE
            normalStateGroup.visibility = View.GONE
        } else {
            emptyStateContainer.visibility = View.GONE
            normalStateGroup.visibility = View.VISIBLE
            
            // Setup Adapter
            // Limit to 2 subjects for dashboard unless toggled
            val dashboardList = if (isShowingAllSubjects) subjectsList else (if (subjectsList.size > 2) subjectsList.take(2) else subjectsList)
            val adapter = DashboardSubjectAdapter(dashboardList, userGoal)
            dashboardRecyclerView.adapter = adapter

            // Update Toggle Button Text
            val viewAllBtn = findViewById<TextView>(R.id.view_all_subjects)
            viewAllBtn.text = if (isShowingAllSubjects) "View Less" else "View All"
        }
    }

    private fun setupListeners() {



        findViewById<View>(R.id.card_add_subject).setOnClickListener {
            openAddSubjectScreen()
        }

        findViewById<View>(R.id.card_view_analysis).setOnClickListener {
            // Navigate to Analysis or show message
            val bottomNav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
            bottomNav.selectedItemId = R.id.nav_analysis
        }

        findViewById<View>(R.id.view_all_subjects).setOnClickListener {
            isShowingAllSubjects = !isShowingAllSubjects
            updateDashboardUI()
        }

        btnEmptyAddSubject.setOnClickListener {
            openAddSubjectScreen()
        }

        // Bottom Nav Highlight and Labels
        val bottomNav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_dashboard
        bottomNav.labelVisibilityMode = com.google.android.material.navigation.NavigationBarView.LABEL_VISIBILITY_LABELED
        
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> true
                R.id.nav_add -> {
                    startActivity(Intent(this, AddAttendanceActivity::class.java))
                    false // Keep false for 'Add' as it's an action that opens a new screen
                }
                R.id.nav_calendar -> {
                    startActivity(Intent(this, CalendarActivity::class.java))
                    true
                }
                R.id.nav_analysis -> {
                    startActivity(Intent(this, AnalysisActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    false
                }
                else -> false
            }
        }

        findViewById<View>(R.id.fab_add).setOnClickListener {
            startActivity(Intent(this, AddAttendanceActivity::class.java))
        }
    }

    private fun openAddSubjectScreen() {
        val intent = Intent(this, AddSubjectsActivity::class.java)
        intent.putExtra("ATTENDANCE_GOAL", userGoal)
        startActivity(intent)
    }
}