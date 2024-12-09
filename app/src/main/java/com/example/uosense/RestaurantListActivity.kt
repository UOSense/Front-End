package com.example.uosense

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.uosense.adapters.RestaurantListAdapter
import com.example.uosense.databinding.ActivityMainBinding
import com.example.uosense.models.RestaurantListResponse
import android.os.Build
class RestaurantListActivity : AppCompatActivity() {

    private lateinit var restaurantList: List<RestaurantListResponse>
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RestaurantListAdapter
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_list)

        binding = ActivityMainBinding.inflate(layoutInflater)
        // 검색 결과 데이터 가져오기
        // 데이터 수신
        // 검색 결과 데이터 가져오기
        restaurantList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra("restaurantList", RestaurantListResponse::class.java)
                ?: emptyList()
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableArrayListExtra("restaurantList") ?: emptyList()
        }

        // RecyclerView 설정
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = RestaurantListAdapter(restaurantList)
        recyclerView.adapter = adapter

        // 필터 버튼 설정
        setupFilterButtons()

        binding.ivMap.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun setupFilterButtons() {
        val buttonFrontGate = findViewById<Button>(R.id.doorTypeButton1)
        val buttonSideGate = findViewById<Button>(R.id.doorTypeButton2)
        val buttonBackGate = findViewById<Button>(R.id.doorTypeButton3)
        val buttonSouthGate = findViewById<Button>(R.id.doorTypeButton4)

        buttonFrontGate.setOnClickListener { filterRestaurants("정문") }
        buttonSideGate.setOnClickListener { filterRestaurants("쪽문") }
        buttonBackGate.setOnClickListener { filterRestaurants("후문") }
        buttonSouthGate.setOnClickListener { filterRestaurants("남문") }
    }

    private fun filterRestaurants(doorType: String) {
        val filteredList = restaurantList.filter { it.doorType == doorType }
        if (filteredList.isNotEmpty()) {
            adapter.updateList(filteredList)
        } else {
            Toast.makeText(this, "$doorType 근처에 검색된 맛집이 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }
}









