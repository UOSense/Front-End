package com.example.uosense.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.uosense.R
import com.example.uosense.RestaurantDetailActivity
import com.example.uosense.models.RestaurantListResponse

class RestaurantListAdapter(private var restaurantList: List<RestaurantListResponse>) :
    RecyclerView.Adapter<RestaurantListAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.restaurantName)
        val categoryTextView: TextView = view.findViewById(R.id.restaurantCategory)
        val ratingTextView: TextView = view.findViewById(R.id.restaurantRating)
        val addressTextView: TextView = view.findViewById(R.id.restaurantAddress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user_restaurant, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val restaurant = restaurantList[position]
        holder.nameTextView.text = restaurant.name
        holder.categoryTextView.text = restaurant.category
        holder.ratingTextView.text = "평점: ${restaurant.rating}"
        holder.addressTextView.text = restaurant.address

        holder.itemView.setOnClickListener {
            val intent = Intent(it.context, RestaurantDetailActivity::class.java).apply {
                putExtra("restaurantId", restaurant.id)
            }
            it.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = restaurantList.size

    // 리스트 갱신 메서드
    fun updateList(newList: List<RestaurantListResponse>) {
        restaurantList = newList
        notifyDataSetChanged()
    }
}
