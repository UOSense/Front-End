package com.example.uosense

import com.example.uosense.AppUtils
import android.content.Intent
import com.example.uosense.MainActivity
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.uosense.adapters.RestaurantListAdapter
import com.example.uosense.models.RestaurantListResponse
import com.example.uosense.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RestaurantListActivity : AppCompatActivity() {

    private lateinit var restaurantList: MutableList<RestaurantListResponse>
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RestaurantListAdapter
    private lateinit var noResultsTextView: TextView
    private var selectedFilter = "DEFAULT"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_list)

        // UI 초기화
        recyclerView = findViewById(R.id.recyclerView)
        noResultsTextView = findViewById(R.id.tvNoResults)

        recyclerView.layoutManager = LinearLayoutManager(this)
        restaurantList = intent.getParcelableArrayListExtra("restaurantList") ?: mutableListOf()





        adapter = RestaurantListAdapter(restaurantList) { navigateToDetailActivity(it) }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter


        checkIfListIsEmpty()

        setupFilterButtons()
        setupSortButton()
    }

    private fun checkIfListIsEmpty() {
        if (restaurantList.isEmpty()) {
            recyclerView.visibility = RecyclerView.GONE
            noResultsTextView.visibility = TextView.VISIBLE
        } else {
            recyclerView.visibility = RecyclerView.VISIBLE
            noResultsTextView.visibility = TextView.GONE
        }
    }


    private fun setupFilterButtons() {
        val buttonFrontGate = findViewById<Button>(R.id.doorTypeButton1)
        val buttonSideGate = findViewById<Button>(R.id.doorTypeButton2)
        val buttonBackGate = findViewById<Button>(R.id.doorTypeButton3)
        val buttonSouthGate = findViewById<Button>(R.id.doorTypeButton4)

        buttonFrontGate.setOnClickListener { fetchFilteredRestaurants("FRONT", "DEFAULT") }
        buttonSideGate.setOnClickListener { fetchFilteredRestaurants("SIDE", "DEFAULT") }
        buttonBackGate.setOnClickListener { fetchFilteredRestaurants("BACK", "DEFAULT") }
        buttonSouthGate.setOnClickListener { fetchFilteredRestaurants("SOUTH", "DEFAULT") }
    }

    private fun fetchFilteredRestaurants(doorType: String, filter: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.restaurantApi.getRestaurantList(doorType, filter)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null && response.body()!!.isNotEmpty()) {
                        adapter.updateList(response.body()!!)
                    } else {
                        adapter.updateList(emptyList())
                    }
                    checkIfListIsEmpty()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RestaurantListActivity, "서버 오류 발생", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun setupSortButton() {
        findViewById<Button>(R.id.btnFilter).setOnClickListener {
            showSortOptions()
        }
    }

    private fun showSortOptions() {
        val sortOptions = arrayOf("리뷰 많은 순", "즐겨찾기 많은 순", "평점 순", "가격 낮은 순", "거리 가까운 순")
        val apiValues = arrayOf("REVIEW", "BOOKMARK", "RATING", "PRICE", "DISTANCE")

        val selectedTextView = findViewById<TextView>(R.id.tvSelectedSort)

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("정렬 기준 선택")
            .setItems(sortOptions) { _, which ->
                selectedFilter = apiValues[which]
                selectedTextView.text = sortOptions[which]
                fetchSortedRestaurants(selectedFilter)
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun fetchSortedRestaurants(sortOption: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.restaurantApi.sortRestaurants(
                    keyword = "",  // 필요 시 검색 키워드 추가
                    filter = sortOption
                )

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null && response.body()!!.isNotEmpty()) {
                        adapter.updateList(response.body()!!)
                    } else {
                        adapter.updateList(emptyList())
                    }
                    checkIfListIsEmpty()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RestaurantListActivity, "정렬 중 오류 발생", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun navigateToDetailActivity(restaurant: RestaurantListResponse) {
        val intent = Intent(this, RestaurantDetailActivity::class.java).apply {
            putExtra("restaurantId", restaurant.id)
        }
        startActivity(intent)
    }

    // 어댑터 업데이트 함수
    fun RestaurantListAdapter.updateList(newList: List<RestaurantListResponse>) {
        restaurantList.clear()
        restaurantList.addAll(newList)
        notifyDataSetChanged()
    }

    private fun setupRecyclerView(restaurantList: List<RestaurantListResponse>) {
        val mutableRestaurantList = restaurantList.toMutableList()

        // 어댑터 초기화 시 클릭 리스너 추가
        val adapter = RestaurantListAdapter(mutableRestaurantList) { selectedRestaurant ->
            val intent = Intent(this, RestaurantDetailActivity::class.java).apply {
                putExtra("restaurantId", selectedRestaurant.id)
            }
            startActivity(intent)
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }



}
