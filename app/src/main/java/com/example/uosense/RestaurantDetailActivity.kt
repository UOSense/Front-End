package com.example.uosense

import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.uosense.adapters.BusinessDayAdapter
import com.example.uosense.models.BusinessDay
import com.example.uosense.models.RestaurantInfo
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
            if (restaurantInfo != null) {
                nameTextView.text = restaurantInfo.name
                addressTextView.text = restaurantInfo.address
                categoryTextView.text = restaurantInfo.category ?: "카테고리 정보 없음"
                descriptionTextView.text = restaurantInfo.description ?: "설명 없음"
                ratingTextView.text = "평점: ${restaurantInfo.rating ?: "정보 없음"}"
                phoneNumberTextView.text =
                    "전화번호: ${restaurantInfo.phoneNumber ?: "전화번호 정보 없음"}"
                businessDayAdapter.submitList(restaurantInfo.businessDays ?: emptyList())
            }
        }

        // 서버 연동을 위한 코드 (주석 처리)
        // if (restaurantId != -1) {
        //     viewModel.fetchRestaurantById(restaurantId)
        // } else {
        //     finish() // 유효하지 않은 ID일 경우 액티비티 종료
        // }

        // Mock 데이터 사용
        loadMockRestaurantDetails(restaurantId)
    }

    private fun loadMockRestaurantDetails(restaurantId: Int) {
        // Mock 데이터
        val mockBusinessDays = listOf(
            BusinessDay(
                id = 1,
                restaurant_id = restaurantId,
                day_of_week = "Monday",
                have_break_time = true,
                start_break_time = "15:00",
                stop_break_time = "17:00",
                opening_time = "09:00",
                closing_time = "21:00",
                is_holiday = false
            ),
            BusinessDay(
                id = 2,
                restaurant_id = restaurantId,
                day_of_week = "Tuesday",
                have_break_time = false,
                start_break_time = null,
                stop_break_time = null,
                opening_time = "10:00",
                closing_time = "22:00",
                is_holiday = false
            )
        )

        val mockRestaurant = RestaurantInfo(
            id = restaurantId,
            name = "Mock Restaurant $restaurantId",
            doorType = "정문",
            latitude = 37.5834643,
            longitude = 127.0536246,
            address = "서울시 중랑구 Mock 주소 $restaurantId",
            phoneNumber = "010-1234-567$restaurantId",
            rating = 4.5,
            category = "Mock 카테고리",
            subDescription = null,
            description = "Mock Restaurant Description",
            reviewCount = 10,
            bookmarkCount = 5,
            businessDays = mockBusinessDays
        )

        // ViewModel을 통해 데이터 업데이트
        viewModel.updateMockData(mockRestaurant)
    }
}

