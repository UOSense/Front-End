package com.example.uosense

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.uosense.adapters.ReviewAdapter
import com.example.uosense.models.ReviewItem
import com.example.uosense.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReviewListActivity : AppCompatActivity() {

    private lateinit var reviewRecyclerView: RecyclerView
    private lateinit var reviewAdapter: ReviewAdapter
    private val reviews = mutableListOf<ReviewItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review_list)

        reviewRecyclerView = findViewById(R.id.reviewRecyclerView)
        reviewRecyclerView.layoutManager = LinearLayoutManager(this)
        reviewAdapter = ReviewAdapter(reviews)
        reviewRecyclerView.adapter = reviewAdapter

        fetchReviews()
    }

    private fun fetchReviews() {
        val restaurantId = intent.getIntExtra("restaurantId", 1) // 임시 restaurantId
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = RetrofitInstance.restaurantApi.getRestaurantReviews(restaurantId)
                if (response.isSuccessful) {
                    val reviewList = response.body() ?: emptyList()
                    reviews.clear()
                    reviews.addAll(reviewList)
                    reviewAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(
                        this@ReviewListActivity,
                        "Failed to fetch reviews: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@ReviewListActivity,
                    "Error occurred: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

