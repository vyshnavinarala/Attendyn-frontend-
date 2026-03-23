package com.simats.attendyn

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.simats.attendyn.model.Subject
import java.util.Locale

class SubjectAdapter(
    private val subjects: List<Subject>,
    private val userGoal: Int
) : RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder>() {

    class SubjectViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.subject_name)
        val statsText: TextView = view.findViewById(R.id.subject_stats)
        val percentageBadge: TextView = view.findViewById(R.id.percentage_badge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_subject, parent, false)
        return SubjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        val subject = subjects[position]
        holder.nameText.text = subject.name
        holder.statsText.text = String.format("Conducted: %d\nAttended: %d", subject.conducted, subject.attended)
        
        val percentage = subject.percentage
        holder.percentageBadge.text = String.format(Locale.US, "%.1f%%", percentage)

        val context = holder.itemView.context
        when {
            percentage >= userGoal -> {
                holder.percentageBadge.setBackgroundResource(R.drawable.bg_badge_green)
                holder.percentageBadge.setTextColor(ContextCompat.getColor(context, R.color.white))
            }
            percentage >= (userGoal - 10) -> {
                holder.percentageBadge.setBackgroundResource(R.drawable.bg_badge_orange)
                holder.percentageBadge.setTextColor(ContextCompat.getColor(context, R.color.white))
            }
            else -> {
                holder.percentageBadge.setBackgroundResource(R.drawable.bg_badge_red)
                holder.percentageBadge.setTextColor(ContextCompat.getColor(context, R.color.white))
            }
        }
    }

    override fun getItemCount() = subjects.size
}
