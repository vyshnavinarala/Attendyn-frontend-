package com.simats.attendyn

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.simats.attendyn.model.Subject
import com.simats.attendyn.model.NameRequest
import com.simats.attendyn.model.CommonResponse
import com.simats.attendyn.network.RetrofitClient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ManageSubjectsActivity : AppCompatActivity() {

    private lateinit var rvSubjects: RecyclerView
    private lateinit var fabAddSubject: com.google.android.material.floatingactionbutton.FloatingActionButton
    private lateinit var btnBack: ImageView
    
    private val subjectsList = mutableListOf<Subject>()
    private lateinit var adapter: ManageSubjectAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_subjects)

        initViews()
        loadSubjects()
        setupRecyclerView()
        setupListeners()
    }

    private fun initViews() {
        rvSubjects = findViewById(R.id.rv_manage_subjects)
        fabAddSubject = findViewById(R.id.fab_add_subject)
        btnBack = findViewById(R.id.btn_back)
    }

    private fun loadSubjects() {
        val prefs = getSharedPreferences("AttendynPrefs", MODE_PRIVATE)
        val token = prefs.getString("AUTH_TOKEN", null)

        if (token == null) {
            loadLocalSubjects()
            return
        }

        RetrofitClient.instance.getSubjects("Bearer $token")
            .enqueue(object : Callback<List<Subject>> {
                override fun onResponse(call: Call<List<Subject>>, response: Response<List<Subject>>) {
                    if (response.isSuccessful) {
                        response.body()?.let { savedSubjects ->
                            subjectsList.clear()
                            subjectsList.addAll(savedSubjects)
                            adapter.notifyDataSetChanged()
                            saveSubjects() // Update local cache
                        }
                    } else {
                        loadLocalSubjects()
                    }
                }

                override fun onFailure(call: Call<List<Subject>>, t: Throwable) {
                    loadLocalSubjects()
                    Toast.makeText(this@ManageSubjectsActivity, "Offline: showing local data", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun loadLocalSubjects() {
        val prefs = getSharedPreferences("AttendynPrefs", MODE_PRIVATE)
        val json = prefs.getString("SUBJECTS_JSON", null)
        if (json != null) {
            val gson = Gson()
            val type = object : TypeToken<MutableList<Subject>>() {}.type
            val savedSubjects: MutableList<Subject> = gson.fromJson(json, type)
            subjectsList.clear()
            subjectsList.addAll(savedSubjects)
            adapter.notifyDataSetChanged()
        }
    }

    private fun setupRecyclerView() {
        adapter = ManageSubjectAdapter(
            subjectsList,
            onEdit = { subject, position -> showEditDialog(subject, position) },
            onDelete = { subject, position -> showDeleteConfirmDialog(subject, position) }
        )
        rvSubjects.layoutManager = LinearLayoutManager(this)
        rvSubjects.adapter = adapter
    }

    private fun setupListeners() {
        fabAddSubject.setOnClickListener {
            startActivity(Intent(this, AddSubjectsActivity::class.java))
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun showEditDialog(subject: Subject, position: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Edit Subject Name")
        
        val input = EditText(this)
        input.setText(subject.name)
        builder.setView(input)

        builder.setPositiveButton("Save") { dialog, _ ->
            val newName = input.text.toString().trim()
            if (newName.isEmpty()) {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            if (subject.id == null) {
                Toast.makeText(this, "Error: Subject ID missing", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            val prefs = getSharedPreferences("AttendynPrefs", MODE_PRIVATE)
            val token = prefs.getString("AUTH_TOKEN", null)

            if (token == null) {
                Toast.makeText(this, "Session expired", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            RetrofitClient.instance.updateSubject("Bearer $token", subject.id, NameRequest(newName))
                .enqueue(object : Callback<CommonResponse> {
                    override fun onResponse(call: Call<CommonResponse>, response: Response<CommonResponse>) {
                        if (response.isSuccessful) {
                            val updatedSubject = subject.copy(name = newName)
                            subjectsList[position] = updatedSubject
                            saveSubjects()
                            adapter.notifyItemChanged(position)
                            Toast.makeText(this@ManageSubjectsActivity, "Updated successfully", Toast.LENGTH_SHORT).show()
                        } else {
                            val error = response.errorBody()?.string() ?: "Failed to update"
                            Toast.makeText(this@ManageSubjectsActivity, error, Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<CommonResponse>, t: Throwable) {
                        Toast.makeText(this@ManageSubjectsActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun showDeleteConfirmDialog(subject: Subject, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Delete Subject")
            .setMessage("Are you sure you want to delete ${subject.name}? All attendance data for this subject will be lost.")
            .setPositiveButton("Delete") { _, _ ->
                if (subject.id == null) {
                    Toast.makeText(this, "Error: Subject ID missing", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val prefs = getSharedPreferences("AttendynPrefs", MODE_PRIVATE)
                val token = prefs.getString("AUTH_TOKEN", null)

                if (token == null) {
                    Toast.makeText(this, "Session expired", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                RetrofitClient.instance.deleteSubject("Bearer $token", subject.id)
                    .enqueue(object : Callback<CommonResponse> {
                        override fun onResponse(call: Call<CommonResponse>, response: Response<CommonResponse>) {
                            if (response.isSuccessful) {
                                subjectsList.removeAt(position)
                                saveSubjects()
                                adapter.notifyItemRemoved(position)
                                Toast.makeText(this@ManageSubjectsActivity, "${subject.name} deleted", Toast.LENGTH_SHORT).show()
                            } else {
                                val error = response.errorBody()?.string() ?: "Failed to delete"
                                Toast.makeText(this@ManageSubjectsActivity, error, Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<CommonResponse>, t: Throwable) {
                            Toast.makeText(this@ManageSubjectsActivity, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveSubjects() {
        val prefs = getSharedPreferences("AttendynPrefs", MODE_PRIVATE)
        val gson = Gson()
        val json = gson.toJson(subjectsList)
        prefs.edit().putString("SUBJECTS_JSON", json).apply()
    }
}
