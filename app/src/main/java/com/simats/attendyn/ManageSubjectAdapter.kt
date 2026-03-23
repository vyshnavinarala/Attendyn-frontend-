package com.simats.attendyn

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.simats.attendyn.model.Subject

class ManageSubjectAdapter(
    private val subjects: MutableList<Subject>,
    private val onEdit: (Subject, Int) -> Unit,
    private val onDelete: (Subject, Int) -> Unit
) : RecyclerView.Adapter<ManageSubjectAdapter.SubjectViewHolder>() {

    class SubjectViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tv_subject_name)
        val tvStats: TextView = view.findViewById(R.id.tv_subject_stats)
        val btnEdit: ImageView = view.findViewById(R.id.btn_edit_subject)
        val btnDelete: ImageView = view.findViewById(R.id.btn_delete_subject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_manage_subject, parent, false)
        return SubjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        val subject = subjects[position]
        holder.tvName.text = subject.name
        holder.tvStats.text = "${subject.attended}/${subject.conducted} classes"

        holder.btnEdit.setOnClickListener { onEdit(subject, position) }
        holder.btnDelete.setOnClickListener { onDelete(subject, position) }
    }

    override fun getItemCount() = subjects.size
}
