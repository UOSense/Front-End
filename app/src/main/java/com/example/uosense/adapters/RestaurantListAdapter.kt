package com.example.uosense.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.uosense.R
import com.example.uosense.models.RestaurantListResponse

class RestaurantListAdapter(
    private var restaurantList: MutableList<RestaurantListResponse>,
    private val onItemClick: (RestaurantListResponse) -> Unit
) : RecyclerView.Adapter<RestaurantListAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val restaurantName: TextView = itemView.findViewById(R.id.restaurantName)
        private val restaurantCategory: TextView = itemView.findViewById(R.id.restaurantCategory)
        private val restaurantAddress: TextView = itemView.findViewById(R.id.restaurantAddress)
        private val restaurantRating: TextView = itemView.findViewById(R.id.restaurantRating)
        private val restaurantImage: ImageView = itemView.findViewById(R.id.restaurantImage)

        fun bind(restaurant: RestaurantListResponse) {
            restaurantName.text = restaurant.name
            restaurantCategory.text = restaurant.category
            restaurantAddress.text = restaurant.address
            restaurantRating.text = restaurant.rating.toString()

            // Glide로 이미지 로딩
            Glide.with(itemView.context)
                .load(restaurant.restaurantImage)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.ic_uos)
                .into(restaurantImage)
            // 클릭 리스너 설정
            itemView.setOnClickListener {
                onItemClick(restaurant)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_restaurant, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(restaurantList[position])
    }

    override fun getItemCount(): Int = restaurantList.size

    // **updateList() 메서드 추가**
    fun updateList(newList: List<RestaurantListResponse>) {
        restaurantList.clear()
        restaurantList.addAll(newList)
        notifyDataSetChanged()
    }
}
