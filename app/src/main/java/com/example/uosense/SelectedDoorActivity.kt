package com.example.uosense

import android.content.Intent
import android.os.Build
import android.os.Bundle
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

class SelectedDoorActivity : AppCompatActivity() {

    private lateinit var restaurantList: MutableList<RestaurantListResponse>
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RestaurantListAdapter
    private lateinit var noResultsTextView: TextView
    private var doorType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selected_door)

        // 초기화
        restaurantList = mutableListOf()
        recyclerView = findViewById(R.id.recyclerView)
        noResultsTextView = findViewById(R.id.tvNoResults)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 인텐트로 전달된 doorType 확인
        doorType = intent.getStringExtra("doorType")

        // 어댑터 설정
        adapter = RestaurantListAdapter(restaurantList) { restaurant ->
            navigateToDetailActivity(restaurant)
        }
        recyclerView.adapter = adapter

        fetchRestaurantsByDoorType(doorType)
    }

    private fun fetchRestaurantsByDoorType(doorType: String?) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.restaurantApi.getRestaurantList(doorType, "DEFAULT")
                withContext(Dispatchers.Main) {
                    adapter.updateList(response)
                    checkIfListIsEmpty()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("데이터 로드 오류 발생")
                }
            }
        }
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

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
