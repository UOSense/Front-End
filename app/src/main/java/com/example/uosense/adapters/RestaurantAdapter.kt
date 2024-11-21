package com.example.uosense.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.uosense.R
import com.example.uosense.models.Restaurant

class RestaurantAdapter( // admin adapter
    private val onEditClick: (Restaurant) -> Unit, // 수정 버튼 클릭 처리
    private val onDeleteClick: (Restaurant) -> Unit // 삭제 버튼 클릭 처리
) : ListAdapter<Restaurant, RestaurantAdapter.RestaurantViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_restaurant, parent, false)
        return RestaurantViewHolder(view)
    }

    override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int) {
        val restaurant = getItem(position)
        holder.bind(restaurant)
    }

    inner class RestaurantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.tvName)
        private val locationTextView: TextView = itemView.findViewById(R.id.tvLocation)
        private val ratingTextView: TextView = itemView.findViewById(R.id.tvRating)
        private val editButton: Button = itemView.findViewById(R.id.btnEdit)
        private val deleteButton: Button = itemView.findViewById(R.id.btnDelete)

        fun bind(restaurant: Restaurant) {
            nameTextView.text = restaurant.name
            locationTextView.text = restaurant.location
            ratingTextView.text = "Rating: ${restaurant.rating}"

            editButton.setOnClickListener {
                onEditClick(restaurant) // 수정 버튼 클릭 처리
            }

            deleteButton.setOnClickListener {
                onDeleteClick(restaurant) // 삭제 버튼 클릭 처리
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Restaurant>() {
            override fun areItemsTheSame(oldItem: Restaurant, newItem: Restaurant): Boolean {
                return oldItem.id == newItem.id // ID가 같으면 동일한 항목
            }

            override fun areContentsTheSame(oldItem: Restaurant, newItem: Restaurant): Boolean {
                return oldItem == newItem // 내용이 같으면 동일한 항목
            }
        }
    }
}
