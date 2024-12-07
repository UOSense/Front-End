package com.example.uosense

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.uosense.adapters.RestaurantListAdapter
import com.example.uosense.models.RestaurantListResponse

class RestaurantListActivity : AppCompatActivity() {

    private lateinit var restaurantList: List<RestaurantListResponse>
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RestaurantListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_list)

        // 검색 결과 데이터 가져오기
        restaurantList = intent.getParcelableArrayListExtra<RestaurantListResponse>("restaurantList")
            ?: emptyList()

        // RecyclerView 설정
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = RestaurantListAdapter(restaurantList)
        recyclerView.adapter = adapter

        // 필터 버튼 설정
        setupFilterButtons()
    }

    private fun setupFilterButtons() {
        // 필터 버튼 참조
        val buttonFrontGate = findViewById<Button>(R.id.doorTypeButton1)
        val buttonSideGate = findViewById<Button>(R.id.doorTypeButton2)
        val buttonBackGate = findViewById<Button>(R.id.doorTypeButton3)
        val buttonSouthGate = findViewById<Button>(R.id.doorTypeButton4)

        // 버튼 클릭 이벤트 설정
        buttonFrontGate.setOnClickListener { filterRestaurants("정문") }
        buttonSideGate.setOnClickListener { filterRestaurants("쪽문") }
        buttonBackGate.setOnClickListener { filterRestaurants("후문") }
        buttonSouthGate.setOnClickListener { filterRestaurants("남문") }
    }

    private fun filterRestaurants(doorType: String) {
        // 도어 타입에 따라 필터링된 리스트 생성
        val filteredList = restaurantList.filter { it.doorType == doorType }

        if (filteredList.isNotEmpty()) {
            // RecyclerView 갱신
            adapter.updateList(filteredList)
        } else {
            // 필터링 결과 없음
            Toast.makeText(this, "$doorType 근처에 검색된 맛집이 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }
}








