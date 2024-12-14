package com.example.uosense

import TokenManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.uosense.adapters.ReportAdapter
import com.example.uosense.databinding.ActivityReportBinding
import com.example.uosense.models.ReportResponse
import com.example.uosense.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReportActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReportBinding
    private lateinit var reportAdapter: ReportAdapter
    private lateinit var tokenManager: TokenManager
    private val reports = mutableListOf<ReportResponse>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tokenManager = TokenManager(this)

        setupRecyclerView()
        fetchReports()

        // 뒤로가기 버튼 리스너 설정
        binding.backBtn.setOnClickListener { finish() }
    }

    // 리사이클러 뷰 설정
    private fun setupRecyclerView() {
        reportAdapter = ReportAdapter(reports)
        binding.reportRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ReportActivity)
            adapter = reportAdapter
        }
    }

    // 신고 내역 가져오기
    private fun fetchReports() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                Log.d("ReportActivity", "Fetching reports...")

                // 액세스 토큰 가져오기
                val accessToken = tokenManager.ensureValidAccessToken()
                if (accessToken.isNullOrEmpty()) {
                    showToast("로그인이 필요합니다.")
                    Log.e("ReportActivity", "Access token is empty")
                    return@launch
                }

                // API 요청
                val response = RetrofitInstance.restaurantApi.getReports("Bearer $accessToken")

                if (response.isNotEmpty()) {
                    reports.clear()
                    reports.addAll(response)
                    reportAdapter.notifyDataSetChanged()
                    showToast("리뷰가 신고되었습니다.")
                    Log.d("ReportActivity", "Fetched ${response.size} reports successfully")
                } else {
                    showToast("신고된 리뷰가 없습니다.")
                    Log.d("ReportActivity", "No reports found")
                }
            } catch (e: Exception) {
                showToast("오류 발생: ${e.message}")
                Log.e("ReportActivity", "Error fetching reports", e)
            }
        }
    }

    // 메시지 출력 함수
    private fun showToast(message: String) {
        Toast.makeText(this@ReportActivity, message, Toast.LENGTH_SHORT).show()
    }
}
