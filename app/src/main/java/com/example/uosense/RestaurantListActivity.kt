package com.example.uosense

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.uosense.R
import com.example.uosense.adapters.RestaurantAdapter
import com.example.uosense.models.RestaurantListResponse
import com.example.uosense.viewmodel.RestaurantViewModel

class RestaurantListActivity : AppCompatActivity() {
    private val viewModel: RestaurantViewModel by viewModels()
    private lateinit var restaurantAdapter: RestaurantAdapter
    private lateinit var recyclerView: RecyclerView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_list)

//        리사이클러 뷰 초기화
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

//        어댑터 설정
        val adapter = RestaurantAdapter { selectedRestaurant ->
            val intent = Intent(this, RestaurantDetailActivity::class.java).apply {
                putExtra("restaurantId", selectedRestaurant.id)
            }
            startActivity(intent)
        }
        recyclerView.adapter = restaurantAdapter

//        데이터 로드
        loadRestaurants()


        viewModel.restaurantList.observe(this) { restaurants ->
            println("Observed Restaurants: $restaurants")
            adapter.submitList(restaurants)
            adapter.notifyDataSetChanged()
        }

        // Fetch mock data
        viewModel.fetchMockRestaurants()
    }

//    가상 데이터 로드, 실제 데이터는 서버에서 가져옴
    private fun loadRestaurants() {
        val mockData = listOf(
            RestaurantListResponse(
                id = 1,
                name = "실크로드",
                category = "바",
                address = "서울 동대문구 전농로 219",
                door_type = "영업 중",
                phone_number = "010-1234-5678",
                rating = 4.64,
                review_count = 35,
                imageResourceId = R.drawable.ic_launcher_foreground
            ),
            RestaurantListResponse(
                id = 2,
                name = "맛집 A",
                category = "한식",
                address = "서울 동대문구 전농로 218",
                door_type = "영업 중",
                phone_number = "010-1234-5679",
                rating = 4.55,
                review_count = 23,
                imageResourceId = R.drawable.ic_uos
            )
        )
        restaurantAdapter.submitList(mockData)
    }

//    아이템 클릭 시 동작
    private fun onRestaurantClicked(restaurant: RestaurantListResponse){
//        클릭된 식당 처리
//    (새로운 액티비티로 이동)
    }


}






