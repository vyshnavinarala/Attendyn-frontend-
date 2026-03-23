package com.simats.attendyn

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.simats.attendyn.model.Subject
import java.util.*
import kotlin.math.floor
import kotlin.math.ceil

class AnalysisActivity : AppCompatActivity() {

    private lateinit var donutProgress: ProgressBar
    private lateinit var tvStatusBadge: TextView
    private lateinit var tvPercentage: TextView
    private lateinit var tvGoalPercentage: TextView
    private lateinit var tvDonutPercentage: TextView
    private lateinit var tvStatusBadgeContainer: com.google.android.material.card.MaterialCardView
    private lateinit var subjectSpinner: Spinner
    
    private lateinit var tvAiInsightText: TextView
    private lateinit var tvPredictionAttend: TextView
    private lateinit var tvPredictionMiss: TextView
    
    private lateinit var insightCard: com.google.android.material.card.MaterialCardView
    private lateinit var predictionCard: com.google.android.material.card.MaterialCardView
    private lateinit var calculatorCardContainer: com.google.android.material.card.MaterialCardView
    private lateinit var cvCalcIconBg: com.google.android.material.card.MaterialCardView
    private lateinit var ivCalcIcon: ImageView
    private lateinit var tvCalcTitle: TextView
    private lateinit var tvCalcStatus: TextView
    private lateinit var tvCalcResultHeaderNumber: TextView
    private lateinit var tvCalcResultHeaderLabel: TextView
    private lateinit var tvCalcResultDesc: TextView
    private lateinit var llTipBox: LinearLayout
    private lateinit var ivTipIcon: ImageView
    private lateinit var tvCalcTip: TextView
    private lateinit var emptyStateContainer: View
    private lateinit var normalStateGroup: androidx.constraintlayout.widget.Group


    private var subjects: List<Subject> = listOf()
    private var selectedSubject: Subject? = null
    private var userGoal: Int = 75
    private var preSelectedSubjectName: String? = null

    companion object {
        const val EXTRA_SUBJECT_NAME = "EXTRA_SUBJECT_NAME"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analysis)

        initViews()
        preSelectedSubjectName = intent.getStringExtra(EXTRA_SUBJECT_NAME)
        loadSubjects()
        setupBottomNavigation()
        setupBackNavigation()
    }

    private fun setupBackNavigation() {
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@AnalysisActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                finish()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        val bottomNav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
        if (bottomNav.selectedItemId != R.id.nav_analysis) {
            bottomNav.selectedItemId = R.id.nav_analysis
        }
        
        loadSubjects()
        updateUI()
    }

    private fun initViews() {
        donutProgress = findViewById(R.id.donut_progress)
        tvStatusBadge = findViewById(R.id.tv_status_badge)
        tvStatusBadgeContainer = findViewById(R.id.tv_status_badge_container)
        tvPercentage = findViewById(R.id.tv_percentage)
        tvDonutPercentage = findViewById(R.id.tv_donut_percentage)
        tvGoalPercentage = findViewById(R.id.tv_goal_percentage)
        subjectSpinner = findViewById(R.id.subject_spinner)
        
        tvAiInsightText = findViewById(R.id.tv_ai_insight_text)
        tvPredictionAttend = findViewById(R.id.tv_prediction_attend)
        tvPredictionMiss = findViewById(R.id.tv_prediction_miss)
        
        insightCard = findViewById(R.id.insight_card)
        predictionCard = findViewById(R.id.prediction_section_card)
        calculatorCardContainer = findViewById(R.id.calculator_card_container)
        cvCalcIconBg = findViewById(R.id.cv_calc_icon_bg)
        ivCalcIcon = findViewById(R.id.iv_calc_icon)
        tvCalcTitle = findViewById(R.id.tv_calc_title)
        tvCalcStatus = findViewById(R.id.tv_calc_status)
        tvCalcResultHeaderNumber = findViewById(R.id.tv_calc_result_header_number)
        tvCalcResultHeaderLabel = findViewById(R.id.tv_calc_result_header_label)
        tvCalcResultDesc = findViewById(R.id.tv_calc_result_desc)
        llTipBox = findViewById(R.id.ll_tip_box)
        ivTipIcon = findViewById(R.id.iv_tip_icon)
        tvCalcTip = findViewById(R.id.tv_calc_tip)
        emptyStateContainer = findViewById(R.id.empty_state_container)
        normalStateGroup = findViewById(R.id.normal_state_group)
    }

    private fun loadSubjects() {
        val sharedPrefs = getSharedPreferences("AttendynPrefs", MODE_PRIVATE)
        val subjectsJson = sharedPrefs.getString("SUBJECTS_JSON", null)
        userGoal = sharedPrefs.getInt("ATTENDANCE_GOAL", 75).coerceAtLeast(1) // Prevent division by zero
        tvGoalPercentage.text = "Goal: $userGoal%"

        if (subjectsJson != null) {
            val type = object : TypeToken<List<Subject>>() {}.type
            subjects = Gson().fromJson(subjectsJson, type)
            
            // Remove listener before setting adapter to prevent unwanted selection triggers
            subjectSpinner.onItemSelectedListener = null
            
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, subjects.map { it.name })
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            subjectSpinner.adapter = adapter

            // Determine what should be selected
            if (selectedSubject == null) {
                // First time load - check for pre-selection from Dashboard
                if (preSelectedSubjectName != null) {
                    selectedSubject = subjects.find { it.name == preSelectedSubjectName }
                    // Clear pre-selection after first use to allow manual changes later
                    preSelectedSubjectName = null 
                }
                
                // Fallback to first subject if none selected or found
                if (selectedSubject == null && subjects.isNotEmpty()) {
                    selectedSubject = subjects[0]
                }
            } else {
                // Refresh existing selection (e.g., when returning from another screen)
                selectedSubject = subjects.find { it.name == selectedSubject?.name } ?: subjects.getOrNull(0)
            }

            // Sync Spinner UI with selectedSubject
            selectedSubject?.let { sub ->
                val index = subjects.indexOfFirst { it.name == sub.name }
                if (index >= 0) {
                    subjectSpinner.setSelection(index, false)
                }
            }

            // Restore/Setup Listener for manual changes
            subjectSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val newSubject = subjects[position]
                    if (selectedSubject?.name != newSubject.name) {
                        selectedSubject = newSubject
                        updateUI()
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
            
            updateUI()
        } else {
            subjects = listOf()
        }

        // Toggle Empty State Visibility
        if (subjects.isEmpty()) {
            emptyStateContainer.visibility = View.VISIBLE
            normalStateGroup.visibility = View.GONE
        } else {
            emptyStateContainer.visibility = View.GONE
            normalStateGroup.visibility = View.VISIBLE
        }
    }

    private fun updateUI() {
        if (subjects.isEmpty()) return
        val subject = selectedSubject ?: return
        
        // Progress Donut
        donutProgress.progress = Math.round(subject.percentage).toInt()
        tvPercentage.text = String.format(Locale.US, "%.1f%%", subject.percentage)
        tvDonutPercentage.text = String.format(Locale.US, "%.1f%%", subject.percentage)
        
        // Validation: No attendance data
        if (subject.conducted == 0) {
            tvStatusBadge.text = "No Data"
            tvStatusBadgeContainer.setCardBackgroundColor(ContextCompat.getColor(this, R.color.attendance_orange))
            tvAiInsightText.text = "No attendance data available."
            insightCard.visibility = View.VISIBLE
            predictionCard.visibility = View.GONE
            calculatorCardContainer.visibility = View.GONE
            return
        }

        // Normal state
        predictionCard.visibility = View.VISIBLE
        calculatorCardContainer.visibility = View.VISIBLE
        insightCard.visibility = View.VISIBLE

        // Status & Insight (Synchronized with Dashboard)
        val riskColor: Int
        val riskLabel: String
        val badgeBg: Int

        when {
            subject.percentage >= userGoal -> {
                riskColor = ContextCompat.getColor(this, R.color.attendance_green)
                riskLabel = "Safe"
                badgeBg = R.drawable.bg_badge_green
            }
            subject.percentage >= (userGoal - 10) -> {
                riskColor = ContextCompat.getColor(this, R.color.attendance_orange)
                riskLabel = "Medium Risk"
                badgeBg = R.drawable.bg_badge_orange
            }
            else -> {
                riskColor = ContextCompat.getColor(this, R.color.attendance_red)
                riskLabel = "High Risk"
                badgeBg = R.drawable.bg_badge_red
            }
        }

        tvStatusBadge.text = riskLabel
        tvStatusBadgeContainer.setCardBackgroundColor(riskColor)
        
        // Update ProgressBar Color (Dashboard Style)
        donutProgress.progressTintList = ColorStateList.valueOf(riskColor)

        // AI Insight Text (Descriptive)
        if (subject.percentage >= userGoal) {
            val diff = subject.percentage - userGoal
            if (userGoal == 100) {
                 tvAiInsightText.text = "Flawless! You have a perfect 100% attendance. Keep it up!"
            } else {
                val goalDecimal = userGoal / 100.0
                val maxCanMiss = floor((subject.attended - goalDecimal * subject.conducted) / goalDecimal).toInt()
                val finalMaxCanMiss = if (maxCanMiss < 0) 0 else maxCanMiss
                
                tvAiInsightText.text = String.format(Locale.US, 
                    "Excellent! Your attendance is in the safe zone. You're %.1f%% above your goal and can afford to miss %d classes safely.",
                    diff, finalMaxCanMiss)
            }
        } else {
            val diff = userGoal - subject.percentage
            if (userGoal == 100) {
                tvAiInsightText.text = String.format(Locale.US,
                    "You can't reach 100%% after missing a class. You're currently at %.1f%%. Aim for a high but realistic goal!",
                    subject.percentage)
            } else {
                tvAiInsightText.text = String.format(Locale.US,
                    "Be careful! You're %.1f%% below your goal. Missing the next class may reduce your attendance further. Stay focused!",
                    diff)
            }
        }

        // Predictions (Strict Formula)
        val nextAttend = (subject.attended + 1).toDouble() / (subject.conducted + 1) * 100
        val nextMiss = subject.attended.toDouble() / (subject.conducted + 1) * 100
        
        tvPredictionAttend.text = String.format(Locale.US, "%.1f%%", nextAttend)
        tvPredictionMiss.text = String.format(Locale.US, "%.1f%%", nextMiss)

        // Calculator Logic (Strict Formulas)
        updateCalculator(subject)
    }

    private fun updateCalculator(subject: Subject) {
        val goalDecimal = userGoal / 100.0
        
        if (subject.percentage >= userGoal) {
            // Safe Leave Container
            // Safe Classes = floor((Attended − Goal × Conducted) / Goal)
            val safeClasses = floor((subject.attended - goalDecimal * subject.conducted) / goalDecimal).toInt()
            val finalSafeClasses = if (safeClasses < 0) 0 else safeClasses
            
            // Apply Safe Styles
            calculatorCardContainer.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.white))
            calculatorCardContainer.strokeColor = ContextCompat.getColor(this, R.color.analysis_prediction_green_border)
            cvCalcIconBg.setCardBackgroundColor(ContextCompat.getColor(this, R.color.analysis_safe_leave_bg))
            ivCalcIcon.setImageResource(R.drawable.ic_security)
            ivCalcIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.attendance_green))
            
            tvCalcTitle.text = "Safe Leave Calculator"
            tvCalcTitle.setTextColor(ContextCompat.getColor(this, R.color.analysis_calc_text_sub))
            
            if (finalSafeClasses <= 0) {
                tvCalcStatus.text = "No safe leave available."
                tvCalcResultHeaderNumber.text = "0"
            } else {
                tvCalcStatus.text = "You're in the safe zone!"
                tvCalcResultHeaderNumber.text = finalSafeClasses.toString()
            }
            
            tvCalcResultHeaderNumber.visibility = View.VISIBLE
            tvCalcResultHeaderLabel.visibility = View.VISIBLE
            tvCalcStatus.setTextColor(ContextCompat.getColor(this, R.color.analysis_calc_accent))
            
            tvCalcResultDesc.text = if (finalSafeClasses > 0) 
                "You can safely miss $finalSafeClasses more classes and still stay above your $userGoal% goal."
                else "You cannot afford to miss any classes right now."
            
            llTipBox.setBackgroundResource(R.drawable.bg_calc_tip_green)
            ivTipIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.analysis_calc_accent))
            tvCalcTip.text = "Maintain consistent attendance to keep this buffer. Every class counts!"
            tvCalcTip.setTextColor(ContextCompat.getColor(this, R.color.analysis_calc_text_sub))
        } else {
            // Classes Needed Container
            // Classes Needed = ceil((Goal × Conducted − Attended) / (1 − Goal))
            
            // Apply Warning Styles
            calculatorCardContainer.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.white))
            calculatorCardContainer.strokeColor = ContextCompat.getColor(this, R.color.analysis_warning_tip_bg)
            cvCalcIconBg.setCardBackgroundColor(ContextCompat.getColor(this, R.color.analysis_warning_bg))
            ivCalcIcon.setImageResource(R.drawable.ic_target_icon)
            ivCalcIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.attendance_orange))
            
            tvCalcTitle.text = "Classes Needed"
            tvCalcTitle.setTextColor(ContextCompat.getColor(this, R.color.analysis_warning_text_sub))
            tvCalcStatus.text = if (userGoal == 100) "Goal Unreachable" else "To reach safe zone"
            tvCalcStatus.setTextColor(ContextCompat.getColor(this, R.color.analysis_warning_accent))
            
            if (userGoal == 100) {
                tvCalcResultHeaderNumber.visibility = View.GONE
                tvCalcResultHeaderLabel.visibility = View.GONE
                tvCalcResultDesc.text = "You can't reach 100% after missing a class."
                tvCalcTip.text = "Set a high but realistic goal like 75% or 90% instead."
            } else {
                tvCalcResultHeaderNumber.visibility = View.VISIBLE
                tvCalcResultHeaderLabel.visibility = View.VISIBLE
                val mustAttend = ceil((goalDecimal * subject.conducted - subject.attended) / (1.0 - goalDecimal)).toInt()
                val finalMustAttend = if (mustAttend < 0) 0 else mustAttend
                tvCalcResultHeaderNumber.text = finalMustAttend.toString()
                tvCalcResultDesc.text = "Attend next $finalMustAttend classes consecutively to reach your $userGoal% goal."
                tvCalcTip.text = "Stay consistent! Every attended class brings you closer to your goal."
            }
            
            tvCalcResultHeaderNumber.setTextColor(ContextCompat.getColor(this, R.color.analysis_warning_accent))
            tvCalcResultHeaderLabel.setTextColor(ContextCompat.getColor(this, R.color.analysis_warning_accent))
            
            llTipBox.setBackgroundResource(R.drawable.bg_calc_tip_orange)
            ivTipIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.analysis_warning_accent))
            tvCalcTip.setTextColor(ContextCompat.getColor(this, R.color.analysis_warning_text_sub))
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_analysis
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
                R.id.nav_add -> {
                    startActivity(Intent(this, AddAttendanceActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_analysis -> true
                else -> false
            }
        }

        findViewById<View>(R.id.fab_add).setOnClickListener {
            startActivity(Intent(this, AddAttendanceActivity::class.java))
            finish()
        }
    }
}
