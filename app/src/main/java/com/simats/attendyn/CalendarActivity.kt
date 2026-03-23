package com.simats.attendyn

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.simats.attendyn.model.Subject
import java.text.SimpleDateFormat
import java.util.*

class CalendarActivity : AppCompatActivity() {

    private lateinit var currentMonth: Calendar
    private lateinit var tvMonthYear: TextView
    private lateinit var rvCalendar: RecyclerView
    private lateinit var adapter: CalendarAdapter
    private lateinit var subjectSpinner: Spinner
    private lateinit var emptyStateContainer: View
    private lateinit var normalStateGroup: androidx.constraintlayout.widget.Group


    private var subjects: List<Subject> = listOf()
    private var selectedSubject: Subject? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        initViews()
        currentMonth = Calendar.getInstance() // Initialize this before loadSubjects
        loadSubjects()
        setupBottomNavigation()

        updateCalendar()
        findViewById<ImageButton>(R.id.btn_prev_month).setOnClickListener {
            currentMonth.add(Calendar.MONTH, -1)
            updateCalendar()
        }

        findViewById<ImageButton>(R.id.btn_next_month).setOnClickListener {
            currentMonth.add(Calendar.MONTH, 1)
            updateCalendar()
        }
    }

    override fun onResume() {
        super.onResume()
        val bottomNav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
        if (bottomNav.selectedItemId != R.id.nav_calendar) {
            bottomNav.selectedItemId = R.id.nav_calendar
        }
        
        loadSubjects()
        updateCalendar()
    }

    private fun initViews() {
        tvMonthYear = findViewById(R.id.tv_month_year)
        rvCalendar = findViewById(R.id.rv_calendar)
        subjectSpinner = findViewById(R.id.subject_spinner)
        emptyStateContainer = findViewById(R.id.empty_state_container)
        normalStateGroup = findViewById(R.id.normal_state_group)

        rvCalendar.layoutManager = GridLayoutManager(this, 7)
    }

    private fun loadSubjects() {
        val sharedPrefs = getSharedPreferences("AttendynPrefs", MODE_PRIVATE)
        val subjectsJson = sharedPrefs.getString("SUBJECTS_JSON", null)
        if (subjectsJson != null) {
            val type = object : TypeToken<List<Subject>>() {}.type
            subjects = Gson().fromJson(subjectsJson, type)
            
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, subjects.map { it.name })
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            subjectSpinner.adapter = adapter

            subjectSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val newSubject = subjects[position]
                    if (selectedSubject?.name != newSubject.name) {
                        selectedSubject = newSubject
                        updateCalendar()
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
            
            if (selectedSubject == null && subjects.isNotEmpty()) {
                selectedSubject = subjects[0]
                updateCalendar()
            } else if (selectedSubject != null) {
                // Refresh the selected subject from the new list
                selectedSubject = subjects.find { it.name == selectedSubject?.name } ?: subjects.getOrNull(0)
            }
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

    private fun updateCalendar() {
        if (subjects.isEmpty()) return
        val subject = selectedSubject ?: return
        val sdfMonth = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        tvMonthYear.text = sdfMonth.format(currentMonth.time)

        val days = mutableListOf<CalendarDate>()
        val monthCopy = currentMonth.clone() as Calendar
        monthCopy.set(Calendar.DAY_OF_MONTH, 1)
        
        val firstDayOfWeek = monthCopy.get(Calendar.DAY_OF_WEEK) - 1
        val daysInMonth = monthCopy.getActualMaximum(Calendar.DAY_OF_MONTH)

        // Add empty days for padding
        for (i in 0 until firstDayOfWeek) {
            days.add(CalendarDate("", 0))
        }

        val sdfKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        for (i in 1..daysInMonth) {
            monthCopy.set(Calendar.DAY_OF_MONTH, i)
            val dateKey = sdfKey.format(monthCopy.time)
            val status = subject.attendanceHistory[dateKey] ?: 0
            days.add(CalendarDate(i.toString(), status))
        }

        adapter = CalendarAdapter(days)
        rvCalendar.adapter = adapter
    }

    private fun setupBottomNavigation() {
        val bottomNav: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNav.selectedItemId = R.id.nav_calendar
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    true
                }
                R.id.nav_add -> {
                    startActivity(Intent(this, AddAttendanceActivity::class.java))
                    true
                }
                R.id.nav_calendar -> {
                    // Already on CalendarActivity, no need to start new activity or finish
                    true
                }
                R.id.nav_analysis -> {
                    startActivity(Intent(this, AnalysisActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }

        findViewById<View>(R.id.fab_add).setOnClickListener {
            startActivity(Intent(this, AddAttendanceActivity::class.java))
        }
    }

    data class CalendarDate(val date: String, val status: Int) // 0: None, 1: Present, 2: Absent

    inner class CalendarAdapter(private val days: List<CalendarDate>) : RecyclerView.Adapter<CalendarAdapter.ViewHolder>() {
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvDay: TextView = view.findViewById(R.id.tv_day)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_day, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val day = days[position]
            holder.tvDay.text = day.date
            
            if (day.date.isEmpty()) {
                holder.tvDay.visibility = View.INVISIBLE
            } else {
                holder.tvDay.visibility = View.VISIBLE
                when (day.status) {
                    1 -> {
                        holder.tvDay.setBackgroundResource(R.drawable.bg_calendar_day)
                        holder.tvDay.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#4CAF50"))
                        holder.tvDay.setTextColor(android.graphics.Color.WHITE)
                    }
                    2 -> {
                        holder.tvDay.setBackgroundResource(R.drawable.bg_calendar_day)
                        holder.tvDay.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#F44336"))
                        holder.tvDay.setTextColor(android.graphics.Color.WHITE)
                    }
                    3 -> {
                        holder.tvDay.setBackgroundResource(R.drawable.bg_calendar_day)
                        holder.tvDay.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#FFE082"))
                        holder.tvDay.setTextColor(android.graphics.Color.parseColor("#1A1C2E"))
                    }
                    else -> {
                        holder.tvDay.setBackgroundResource(R.drawable.bg_calendar_day)
                        holder.tvDay.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#F4F4F9"))
                        holder.tvDay.setTextColor(android.graphics.Color.parseColor("#1A1C2E"))
                    }
                }
            }
        }

        override fun getItemCount() = days.size
    }
}
