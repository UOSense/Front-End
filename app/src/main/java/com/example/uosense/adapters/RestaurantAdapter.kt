package com.example.uosense.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.uosense.R
import com.example.uosense.models.Restaurant
import com.example.uosense.models.BusinessDay

class RestaurantAdapter(private val restaurants: List<Restaurant>) :
    RecyclerView.Adapter<RestaurantAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.restaurantName)
        val doorType: TextView = itemView.findViewById(R.id.restaurantDoorType)
        val category: TextView = itemView.findViewById(R.id.restaurantCategory)
        val address: TextView = itemView.findViewById(R.id.restaurantAddress)
        val phoneNumber: TextView = itemView.findViewById(R.id.restaurantPhoneNumber)
        val rating: TextView = itemView.findViewById(R.id.restaurantRating)
        val businessDaysContainer: LinearLayout = itemView.findViewById(R.id.businessDaysContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_restaurant, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val restaurant = restaurants[position]

        // 식당 기본 정보 설정
        holder.name.text = restaurant.name
        holder.doorType.text = "Door Type: ${restaurant.door_type}"
        holder.category.text = "Category: ${restaurant.category} - ${restaurant.sub_description}"
        holder.address.text = "Address: ${restaurant.address}"
        holder.phoneNumber.text = "Phone: ${restaurant.phone_number ?: "N/A"}"
        holder.rating.text = "Rating: ${restaurant.rating}"

        // 영업일(BusinessDay) 리스트 동적 추가
        holder.businessDaysContainer.removeAllViews()
        for (businessDay in restaurant.businessDays) {
            val businessDayView = createBusinessDayView(holder.itemView.context, businessDay)
            holder.businessDaysContainer.addView(businessDayView)
        }
    }

    override fun getItemCount() = restaurants.size

    private fun createBusinessDayView(context: Context, businessDay: BusinessDay): View {
        val view = LayoutInflater.from(context).inflate(R.layout.item_business_day, null, false)

        val dayOfWeek = view.findViewById<TextView>(R.id.dayOfWeek)
        val openingHours = view.findViewById<TextView>(R.id.openingHours)

        dayOfWeek.text = businessDay.day_of_week
        val breakTime = if (businessDay.have_break_time) {
            "Break: ${businessDay.start_break_time} - ${businessDay.stop_break_time}"
        } else {
            "No Break"
        }
        openingHours.text = "Open: ${businessDay.opening_time} - ${businessDay.closing_time} | $breakTime"

        return view
    }
}

