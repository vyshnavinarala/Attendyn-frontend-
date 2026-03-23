package com.simats.attendyn

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.simats.attendyn.model.Subject
import com.simats.attendyn.model.SubjectRequest
import com.simats.attendyn.model.CommonResponse
import com.simats.attendyn.network.RetrofitClient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale

class AddSubjectsActivity : AppCompatActivity() {

    private lateinit var subjectNameEdit: EditText
    private lateinit var conductedEdit: EditText
    private lateinit var attendedEdit: EditText
    private lateinit var calcPercentageText: TextView
    private lateinit var addSubjectButton: AppCompatButton
    private lateinit var continueDashboardButton: AppCompatButton
    private lateinit var subjectsRecyclerView: RecyclerView
    private lateinit var subjectsCountText: TextView
    private lateinit var emptyHint: TextView
    private lateinit var calcCard: View
    private lateinit var subjectsHeader: View

    private val subjectsList = ArrayList<Subject>()
    private lateinit var adapter: SubjectAdapter
    private var userGoal: Int = 75

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_subjects)

        val prefs = getSharedPreferences("AttendynPrefs", MODE_PRIVATE)
        userGoal = intent.getIntExtra("ATTENDANCE_GOAL", prefs.getInt("ATTENDANCE_GOAL", 75))

        initViews()
        setupListeners()
        setupRecyclerView()
        
        val isOnboarding = intent.getBooleanExtra("EXTRA_IS_ONBOARDING", false)
        if (!isOnboarding) {
            loadExistingData()
        }
    }

    private fun loadExistingData() {
        val prefs = getSharedPreferences("AttendynPrefs", MODE_PRIVATE)
        val token = prefs.getString("AUTH_TOKEN", null)

        if (token == null) return

        RetrofitClient.instance.getSubjects("Bearer $token")
            .enqueue(object : Callback<List<Subject>> {
                override fun onResponse(call: Call<List<Subject>>, response: Response<List<Subject>>) {
                    if (response.isSuccessful) {
                        response.body()?.let { savedSubjects ->
                            subjectsList.clear()
                            subjectsList.addAll(savedSubjects)
                            adapter.notifyDataSetChanged()
                            updateSubjectsState()
                            
                            // Update local cache
                            val json = Gson().toJson(savedSubjects)
                            prefs.edit().putString("SUBJECTS_JSON", json).apply()
                        }
                    }
                }

                override fun onFailure(call: Call<List<Subject>>, t: Throwable) {
                    // Fallback to local data if network fails
                    val json = prefs.getString("SUBJECTS_JSON", null)
                    if (json != null) {
                        val gson = Gson()
                        val type = object : TypeToken<ArrayList<Subject>>() {}.type
                        val savedSubjects: ArrayList<Subject> = gson.fromJson(json, type)
                        subjectsList.clear()
                        subjectsList.addAll(savedSubjects)
                        adapter.notifyDataSetChanged()
                        updateSubjectsState()
                    }
                }
            })
    }

    private fun initViews() {
        subjectNameEdit = findViewById(R.id.subject_name_edit)
        conductedEdit = findViewById(R.id.conducted_edit)
        attendedEdit = findViewById(R.id.attended_edit)
        calcPercentageText = findViewById(R.id.calculated_percentage_text)
        addSubjectButton = findViewById(R.id.add_subject_button)
        continueDashboardButton = findViewById(R.id.continue_dashboard_button)
        subjectsRecyclerView = findViewById(R.id.subjects_recycler_view)
        subjectsCountText = findViewById(R.id.subjects_count_text)
        emptyHint = findViewById(R.id.empty_hint)
        calcCard = findViewById(R.id.calc_card)
        subjectsHeader = findViewById(R.id.subjects_section_container)
    }

    private fun setupListeners() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                calculateAndValidate()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        subjectNameEdit.addTextChangedListener(watcher)
        conductedEdit.addTextChangedListener(watcher)
        attendedEdit.addTextChangedListener(watcher)

        addSubjectButton.setOnClickListener {
            addSubject()
        }

        continueDashboardButton.setOnClickListener {
            if (subjectsList.isEmpty()) {
                android.widget.Toast.makeText(this, "Please add at least one subject to continue", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = SubjectAdapter(subjectsList, userGoal)
        subjectsRecyclerView.adapter = adapter
    }

    private fun calculateAndValidate() {
        val name = subjectNameEdit.text.toString().trim()
        val conductedStr = conductedEdit.text.toString()
        val attendedStr = attendedEdit.text.toString()

        val conducted = conductedStr.toIntOrNull() ?: 0
        val attended = attendedStr.toIntOrNull() ?: 0

        // Specific Logic Requested:
        // if conducted <= 0: error
        // elif attended < 0: error
        // elif attended > conducted: error
        // else: percentage = (attended / conducted) * 100, enableAddButton()

        var isValid = false
        val percentage: Double

        if (conductedStr.isNotEmpty() && conducted <= 0) {
            // error: conducted <= 0
            percentage = 0.0
            isValid = false
        } else if (attendedStr.isNotEmpty() && attended < 0) {
            // error: attended < 0
            percentage = 0.0
            isValid = false
        } else if (attendedStr.isNotEmpty() && conductedStr.isNotEmpty() && attended > conducted) {
            // error: attended > conducted
            percentage = 0.0
            isValid = false
            android.widget.Toast.makeText(this, "Attended classes cannot be more than conducted classes", android.widget.Toast.LENGTH_SHORT).show()
        } else if (name.isNotEmpty() && conductedStr.isNotEmpty() && attendedStr.isNotEmpty()) {
            // Valid case
            percentage = (attended.toDouble() / conducted.toDouble()) * 100.0
            isValid = true
        } else {
            // Incomplete input case
            percentage = 0.0
            isValid = false
        }

        calcPercentageText.text = String.format(Locale.US, "%.2f%%", percentage)

        // Show/Hide Calculation Card
        calcCard.visibility = if (isValid) View.VISIBLE else View.GONE

        // Color Logic
        when {
            percentage >= userGoal -> calcPercentageText.setTextColor(ContextCompat.getColor(this, R.color.attendance_green))
            percentage >= (userGoal - 10) -> calcPercentageText.setTextColor(ContextCompat.getColor(this, R.color.attendance_orange))
            else -> calcPercentageText.setTextColor(ContextCompat.getColor(this, R.color.attendance_red))
        }

        addSubjectButton.isEnabled = isValid
        addSubjectButton.alpha = if (isValid) 1.0f else 0.5f
    }

    private fun addSubject() {
        val name = subjectNameEdit.text.toString().trim()
        
        val nameExists = subjectsList.any { it.name.equals(name, ignoreCase = true) }
        
        if (nameExists) {
            android.widget.Toast.makeText(this, "Subject already added", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        val conducted = conductedEdit.text.toString().toInt()
        val attended = attendedEdit.text.toString().toInt()

        val prefs = getSharedPreferences("AttendynPrefs", MODE_PRIVATE)
        val token = prefs.getString("AUTH_TOKEN", null)

        if (token == null) {
            android.widget.Toast.makeText(this, "Session expired", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        addSubjectButton.isEnabled = false
        val request = SubjectRequest(name, conducted, attended)

        RetrofitClient.instance.addSubject("Bearer $token", request)
            .enqueue(object : Callback<CommonResponse> {
                override fun onResponse(call: Call<CommonResponse>, response: Response<CommonResponse>) {
                    addSubjectButton.isEnabled = true
                    if (response.isSuccessful) {
                        val subject = Subject(name = name, conducted = conducted, attended = attended)
                        subjectsList.add(subject)
                        adapter.notifyItemInserted(subjectsList.size - 1)
                        
                        updateSubjectsState()
                        clearInputs()
                    } else {
                        val error = response.errorBody()?.string() ?: "Failed to add subject"
                        android.widget.Toast.makeText(this@AddSubjectsActivity, error, android.widget.Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<CommonResponse>, t: Throwable) {
                    addSubjectButton.isEnabled = true
                    android.widget.Toast.makeText(this@AddSubjectsActivity, "Network Error: ${t.message}", android.widget.Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun updateSubjectsState() {
        val count = subjectsList.size
        subjectsCountText.text = "Added Subjects ($count)"
        
        val hasSubjects = count > 0
        continueDashboardButton.isEnabled = hasSubjects
        continueDashboardButton.alpha = if (hasSubjects) 1.0f else 0.5f
        
        subjectsHeader.visibility = if (hasSubjects) View.VISIBLE else View.GONE
        subjectsRecyclerView.visibility = if (hasSubjects) View.VISIBLE else View.GONE
        emptyHint.visibility = if (hasSubjects) View.GONE else View.VISIBLE
    }

    private fun clearInputs() {
        subjectNameEdit.text.clear()
        conductedEdit.text.clear()
        attendedEdit.text.clear()
        calcPercentageText.text = "0.00%"
        calcPercentageText.setTextColor(ContextCompat.getColor(this, R.color.attendance_red))
        calcCard.visibility = View.GONE
        addSubjectButton.isEnabled = false
        addSubjectButton.alpha = 0.5f
    }
}
