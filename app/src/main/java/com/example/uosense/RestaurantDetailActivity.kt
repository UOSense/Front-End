package com.example.uosense

import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.uosense.adapters.BusinessDayAdapter
import com.example.uosense.viewmodel.RestaurantViewModel

class RestaurantDetailActivity : AppCompatActivity() {
    private val viewModel: RestaurantViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_UOSense) // 테마 설정
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_detail)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        val restaurantId = intent.getIntExtra("restaurantId", -1)

        val nameTextView = findViewById<TextView>(R.id.restaurantName)
        val addressTextView = findViewById<TextView>(R.id.restaurantAddress)
        val categoryTextView = findViewById<TextView>(R.id.restaurantCategory)
        val ratingTextView = findViewById<TextView>(R.id.restaurantRating)
        val descriptionTextView = findViewById<TextView>(R.id.restaurantDescription)
        val phoneNumberTextView = findViewById<TextView>(R.id.restaurantPhoneNumber)
        val businessDaysRecyclerView = findViewById<RecyclerView>(R.id.businessDaysRecyclerView)

        businessDaysRecyclerView.layoutManager = LinearLayoutManager(this)
        val businessDayAdapter = BusinessDayAdapter()
        businessDaysRecyclerView.adapter = businessDayAdapter

        // ViewModel에서 데이터 관찰
        viewModel.restaurantInfo.observe(this) { restaurantInfo ->
            nameTextView.text = restaurantInfo.name
            addressTextView.text = restaurantInfo.address
            categoryTextView.text = restaurantInfo.category
            descriptionTextView.text = restaurantInfo.description ?: "정보 없음"
            ratingTextView.text = "평점: ${restaurantInfo.rating}"
            phoneNumberTextView.text = "전화번호: ${restaurantInfo.phone_number ?: "정보 없음"}"
            businessDayAdapter.submitList(restaurantInfo.businessDays)
        }

        // Fetch restaurant details
        viewModel.fetchRestaurantById(restaurantId)
    }
}

