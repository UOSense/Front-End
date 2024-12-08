package com.example.uosense.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView

import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.uosense.R

import com.example.uosense.models.RestaurantListResponse
import com.bumptech.glide.Glide

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
        val reviewCount: TextView = itemView.findViewById((R.id.restaurantReview))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_restaurant, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val restaurant = getItem(position)

        Glide.with(holder.image.context)
            .load(restaurant.restaurantImage)
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.error_image)
            .into(holder.image)

        holder.name.text = restaurant.name
        holder.category.text = restaurant.category
        holder.address.text = restaurant.address
        holder.doorType.text = restaurant.doorType ?: "미정"
        holder.phoneNumber.text = restaurant.phoneNumber ?: "정보 없음"
        holder.rating.text = "평점: ${restaurant.rating}"
        holder.reviewCount.text = "리뷰: ${restaurant.reviewCount}"
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

