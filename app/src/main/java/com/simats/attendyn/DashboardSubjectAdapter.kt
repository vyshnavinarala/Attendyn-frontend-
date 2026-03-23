package com.simats.attendyn

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.simats.attendyn.model.Subject
import java.util.Locale

class DashboardSubjectAdapter(
    private val subjects: List<Subject>,
    private val userGoal: Int
) : RecyclerView.Adapter<DashboardSubjectAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val subjectName: TextView = view.findViewById(R.id.subject_name)
        val statsText: TextView = view.findViewById(R.id.stats_text)
        val percentageText: TextView = view.findViewById(R.id.percentage_text)
        val riskBadge: TextView = view.findViewById(R.id.risk_badge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dashboard_subject, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val subject = subjects[position]
        holder.subjectName.text = subject.name
        holder.statsText.text = String.format(Locale.US, "%d / %d classes attended", subject.attended, subject.conducted)
        
        val percentage = subject.percentage
        holder.percentageText.text = String.format(Locale.US, "%.1f%%", percentage)

        val context = holder.itemView.context
        val riskColor: Int
        val riskText: String
        val badgeBg: Int

        when {
            percentage >= userGoal -> {
                riskColor = ContextCompat.getColor(context, R.color.attendance_green)
                riskText = "Safe"
                badgeBg = R.drawable.bg_badge_green
            }
            percentage >= (userGoal - 10) -> {
                riskColor = ContextCompat.getColor(context, R.color.attendance_orange)
                riskText = "Medium Risk"
                badgeBg = R.drawable.bg_badge_orange
            }
            else -> {
                riskColor = ContextCompat.getColor(context, R.color.attendance_red)
                riskText = "High Risk"
                badgeBg = R.drawable.bg_badge_red
            }
        }

        holder.riskBadge.text = riskText
        holder.riskBadge.setTextColor(ContextCompat.getColor(context, R.color.white))
        holder.riskBadge.setBackgroundResource(badgeBg)
        holder.percentageText.setTextColor(riskColor)

        // Navigate to Analysis with Subject Selection
        holder.itemView.setOnClickListener {
            val intent = android.content.Intent(context, AnalysisActivity::class.java)
            intent.putExtra("EXTRA_SUBJECT_NAME", subject.name)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = subjects.size
}
