package com.example.uosense

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.uosense.adapters.ReviewAdapter
import com.example.uosense.databinding.ActivityReviewListBinding
import com.example.uosense.models.ReviewResponse
import com.example.uosense.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReviewListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReviewListBinding
    private lateinit var reviewAdapter: ReviewAdapter
    private var restaurantId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        restaurantId = intent.getIntExtra("restaurantId", -1)
        if (restaurantId == -1) {
            Toast.makeText(this, "잘못된 식당 정보입니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupRecyclerView()
        loadReviews()

        binding.backBtn.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        reviewAdapter = ReviewAdapter()
        binding.reviewRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ReviewListActivity)
            adapter = reviewAdapter
        }
    }

    private fun loadReviews() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.restaurantApi.getRestaurantReviews(restaurantId)
                withContext(Dispatchers.Main) {
                    if (response.isNotEmpty()) {
                        reviewAdapter.submitList(response)
                    } else {
                        Toast.makeText(this@ReviewListActivity, "리뷰가 없습니다.", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ReviewListActivity, "리뷰 불러오기 실패", Toast.LENGTH_SHORT)
                        .show()
                }
                Log.e("LOAD_REVIEWS", "오류: ${e.message}", e)
            }
        }
    }
}
