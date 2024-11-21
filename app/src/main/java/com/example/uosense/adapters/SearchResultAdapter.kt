package com.example.uosense.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.uosense.R
import com.example.uosense.models.Restaurant

class SearchResultAdapter(
    private val onItemClick: (Restaurant) -> Unit // 리스트 아이템 클릭 처리
) : ListAdapter<Restaurant, SearchResultAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user_restaurant, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val restaurant = getItem(position)
        holder.bind(restaurant)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.tvName)
        private val locationTextView: TextView = itemView.findViewById(R.id.tvLocation)
        private val ratingTextView: TextView = itemView.findViewById(R.id.tvRating)

        fun bind(restaurant: Restaurant) {
            nameTextView.text = restaurant.name
            locationTextView.text = restaurant.location
            ratingTextView.text = "Rating: ${restaurant.rating}"

            // 아이템 클릭 이벤트
            itemView.setOnClickListener {
                onItemClick(restaurant)
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Restaurant>() {
            override fun areItemsTheSame(oldItem: Restaurant, newItem: Restaurant): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Restaurant, newItem: Restaurant): Boolean {
                return oldItem == newItem
            }
        }
    }
}
