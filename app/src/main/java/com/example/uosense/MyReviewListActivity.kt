package com.example.uosense

import TokenManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
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

class MyReviewListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var reviewAdapter: ReviewAdapter
    private lateinit var backBtn: Button
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_review_list)

        tokenManager = TokenManager(this)

        // UI 초기화
        recyclerView = findViewById(R.id.reviewRecyclerView)
        backBtn = findViewById(R.id.backBtn)
        backBtn.setOnClickListener {
            navigateToMyPage()
        }

        setupRecyclerView()
        fetchMyReviews()
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        reviewAdapter = ReviewAdapter(emptyList()) { holder ->
            holder.reportBtn.visibility = View.GONE
            holder.deleteBtn.visibility = View.VISIBLE
        }
        recyclerView.adapter = reviewAdapter
    }

    private fun fetchMyReviews() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val accessToken = tokenManager.getAccessToken().orEmpty()
                if (accessToken.isEmpty()) {
                    showToast("로그인이 필요합니다.")
                    return@launch
                }

                val reviews = RetrofitInstance.restaurantApi.getMyReviews("Bearer $accessToken")
                if (reviews.isNotEmpty()) {
                    reviewAdapter = ReviewAdapter(reviews){ holder ->
                        holder.reportBtn.visibility = View.GONE
                        holder.deleteBtn.visibility = View.VISIBLE
                    }
                    recyclerView.adapter = reviewAdapter
                    recyclerView.visibility = View.VISIBLE
                } else {
                    showToast("작성한 리뷰가 없습니다.")
                }
            } catch (e: Exception) {
                showToast("오류 발생: ${e.message}")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this@MyReviewListActivity, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToMyPage() {
        val intent = Intent(this, MyPageActivity::class.java)
        startActivity(intent)
        finish() // 현재 액티비티 종료
    }
}
