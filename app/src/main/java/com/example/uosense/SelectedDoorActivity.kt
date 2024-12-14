package com.example.uosense

import TokenManager
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.uosense.adapters.RestaurantListAdapter
import com.example.uosense.databinding.ActivitySelectedDoorBinding
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
    private lateinit var binding: ActivitySelectedDoorBinding

    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectedDoorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)

        binding.ivMap.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()  // 뒤로 가기 액션 실행
        }

        restaurantList = mutableListOf()
        recyclerView = binding.recyclerView
        noResultsTextView = binding.tvNoResults
        recyclerView.layoutManager = LinearLayoutManager(this)

        doorType = intent.getStringExtra("doorType")

        setupRecyclerView()
        fetchRestaurantsByDoorType(doorType)
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = RestaurantListAdapter(
            restaurantList,
            onItemClick = { navigateToDetailActivity(it) },
            onDeleteClick = { restaurant -> confirmDeleteRestaurant(restaurant) }
        )
        binding.recyclerView.adapter = adapter
    }

    private fun confirmDeleteRestaurant(restaurant: RestaurantListResponse) {
        AlertDialog.Builder(this)
            .setTitle("삭제 확인")
            .setMessage("정말로 ${restaurant.name} 식당을 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ -> deleteRestaurant(restaurant.id) }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun deleteRestaurant(restaurantId: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            val accessToken = tokenManager.ensureValidAccessToken()
            if (accessToken.isNullOrEmpty()) {
                showToast("로그인이 필요합니다.")
                return@launch
            }

            try {
                val response = RetrofitInstance.restaurantApi.deleteRestaurant("Bearer $accessToken", restaurantId)
                if (response.isSuccessful) {
                    restaurantList.removeAll { it.id == restaurantId }
                    adapter.updateList(restaurantList)
                    showToast("삭제되었습니다.")
                } else {
                    showToast("삭제 실패.")
                }
            } catch (e: Exception) {
                showToast("삭제 중 오류 발생.")
            }
        }
    }

    private fun fetchRestaurantsByDoorType(doorType: String?) {
        CoroutineScope(Dispatchers.Main).launch {
            val accessToken = tokenManager.ensureValidAccessToken()
            if (accessToken.isNullOrEmpty()) {
                showToast("로그인이 필요합니다.")
                return@launch
            }

            try {
                val response = RetrofitInstance.restaurantApi.getRestaurantList(doorType, "Bearer $accessToken")
                if (response.isSuccessful && response.body() != null) {
                    adapter.updateList(response.body()!!)
                } else {
                    adapter.updateList(emptyList())
                    showToast("식당 목록이 없습니다.")
                }
                checkIfListIsEmpty()
            } catch (e: Exception) {
                showToast("데이터 로드 오류 발생")
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

    private fun RestaurantListAdapter.updateList(newList: List<RestaurantListResponse>) {
        restaurantList.clear()
        restaurantList.addAll(newList)
        notifyDataSetChanged()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
