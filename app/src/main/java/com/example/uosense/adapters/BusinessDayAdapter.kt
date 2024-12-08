package com.example.uosense.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.uosense.R
import com.example.uosense.models.BusinessDay

class BusinessDayAdapter :
    ListAdapter<BusinessDay, BusinessDayAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayOfWeek: TextView = itemView.findViewById(R.id.dayOfWeek)
        val openingTime: TextView = itemView.findViewById(R.id.openingTime)
        val closingTime: TextView = itemView.findViewById(R.id.closingTime)
        val breakTime: TextView = itemView.findViewById(R.id.breakTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_business_day, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val businessDay = getItem(position)
        holder.dayOfWeek.text = businessDay.dayOfWeek
        holder.openingTime.text = "오픈 시간: ${businessDay.openingTime}"
        holder.closingTime.text = "끝나는 시간: ${businessDay.closingTime}"
        holder.breakTime.text = if (businessDay.haveBreakTime) {
            "브레이크 타임: ${businessDay.startBreakTime} - ${businessDay.stopBreakTime}"
        } else {
            "No Break Time"
        }
    }


    class DiffCallback : DiffUtil.ItemCallback<BusinessDay>() {
        override fun areItemsTheSame(oldItem: BusinessDay, newItem: BusinessDay): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: BusinessDay, newItem: BusinessDay): Boolean {
            return oldItem == newItem
        }
    }
}
