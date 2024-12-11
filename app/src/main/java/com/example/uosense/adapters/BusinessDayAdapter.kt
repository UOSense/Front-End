package com.example.uosense.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.uosense.R
import com.example.uosense.models.BusinessDayInfo

class BusinessDayAdapter : RecyclerView.Adapter<BusinessDayAdapter.ViewHolder>() {

    private var businessDays = listOf<BusinessDayInfo>()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayOfWeek: TextView = itemView.findViewById(R.id.dayOfWeek)
        val openingTime: TextView = itemView.findViewById(R.id.openingTime)
        val closingTime: TextView = itemView.findViewById(R.id.closingTime)
        val startBreakTime: TextView = itemView.findViewById(R.id.startBreakTime)
        val stopBreakTime: TextView = itemView.findViewById(R.id.stopBreakTime)
        val holiday: TextView = itemView.findViewById(R.id.holiday)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_business_day, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val businessDay = businessDays[position]
        holder.dayOfWeek.text = businessDay.dayOfWeek
        holder.openingTime.text = businessDay.openingTime.toString()
        holder.closingTime.text = businessDay.closingTime.toString()

        holder.startBreakTime.text = businessDay.startBreakTime ?: "없음"
        holder.stopBreakTime.text = businessDay.stopBreakTime ?: "없음"
        holder.holiday.text = if (businessDay.holiday) "휴일" else "영업 중"
    }

    override fun getItemCount(): Int = businessDays.size

    fun submitList(newBusinessDays: List<BusinessDayInfo>) {
        businessDays = newBusinessDays
        notifyDataSetChanged()
    }
}
