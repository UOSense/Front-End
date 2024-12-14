package com.example.uosense

import android.os.Bundle
import android.util.Log
import android.view.View
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
    private var restaurantId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review_list)
        // 식당 ID 수신
        restaurantId = intent.getIntExtra("restaurantId", -1)

        reviewRecyclerView = findViewById(R.id.reviewRecyclerView)
        reviewRecyclerView.layoutManager = LinearLayoutManager(this)
        reviewAdapter = ReviewAdapter(reviews) { holder ->
            holder.reportBtn.visibility = View.VISIBLE
            holder.deleteBtn.visibility = View.GONE
        }


        reviewRecyclerView.adapter = reviewAdapter

        fetchReviews()
    }

    private fun fetchReviews() {
        val restaurantId = restaurantId
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // 로딩 중 UI 표시 (예: ProgressBar를 보여줌)
                showLoading()

                val response = RetrofitInstance.restaurantApi.getRestaurantReviews(restaurantId)
                if (response.isSuccessful) {
                    val reviewList = response.body() ?: emptyList()

                    // 데이터 확인 로그
                    reviewList.forEach { Log.d("ReviewItem", it.toString()) }



                    if (reviewList.isEmpty()) {
                        Toast.makeText(
                            this@ReviewListActivity,
                            "리뷰가 없습니다.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        reviews.clear()
                        reviews.addAll(reviewList)
                        reviewAdapter.notifyDataSetChanged()
                        Log.d("ReviewList", "Fetched ${reviews.size} reviews successfully")
                    }
                } else {
                    Toast.makeText(
                        this@ReviewListActivity,
                        "리뷰를 가져오는 데 실패했습니다: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@ReviewListActivity,
                    "오류 발생: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                // 로딩 UI 숨기기
                hideLoading()
            }
        }
    }

    private fun showLoading() {
        // 로딩 ProgressBar 또는 다른 로딩 상태 UI 표시
    }

    private fun hideLoading() {
        // 로딩 ProgressBar 숨기기
    }

}

