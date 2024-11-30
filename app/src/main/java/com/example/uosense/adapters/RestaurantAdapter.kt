package com.example.uosense.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.uosense.R
import com.example.uosense.models.Restaurant
import com.example.uosense.models.BusinessDay
import com.example.uosense.models.RestaurantListResponse

class RestaurantAdapter(private val onItemClick: (RestaurantListResponse) -> Unit) :
    ListAdapter<RestaurantListResponse, RestaurantAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.restaurantImage)
        val name: TextView = itemView.findViewById(R.id.restaurantName)
        val category: TextView = itemView.findViewById(R.id.restaurantCategory)
        val address: TextView = itemView.findViewById((R.id.restaurantAddress))
        val doorType: TextView = itemView.findViewById(R.id.restaurantDoorType) // 추가
        val phoneNumber: TextView = itemView.findViewById(R.id.restaurantPhoneNumber) // 추가
        val rating: TextView = itemView.findViewById((R.id.restaurantRating))
        val review_count: TextView = itemView.findViewById((R.id.restaurantReview))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_restaurant, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val restaurant = getItem(position)
        holder.image.setImageResource(restaurant.imageResourceId)
        holder.name.text = restaurant.name
        holder.category.text = restaurant.category
        holder.doorType.text = "구역: ${restaurant.door_type ?: "N/A"}" // 추가
        holder.address.text = restaurant.address
        holder.phoneNumber.text = "전화번호: ${restaurant.phone_number ?: "N/A"}" // 추가
        holder.rating.text = "평점: ${restaurant.rating ?: "N/A"}"
        holder.review_count.text = "리뷰: ${restaurant.review_count ?: "N/A"}"
        holder.itemView.setOnClickListener { onItemClick(restaurant) }
    }

    class DiffCallback : DiffUtil.ItemCallback<RestaurantListResponse>() {
        override fun areItemsTheSame(
            oldItem: RestaurantListResponse, newItem: RestaurantListResponse
        ) = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: RestaurantListResponse, newItem: RestaurantListResponse
        ) = oldItem == newItem
    }
}

