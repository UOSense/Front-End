package com.example.uosense

import BusinessDayAdapter
import TokenManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.uosense.adapters.MenuAdapter
import com.example.uosense.models.BusinessDayInfo
import com.example.uosense.models.MenuResponse
import com.example.uosense.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RestaurantDetailActivity : AppCompatActivity() {

    // 식당 id 임의 선언
    private var restaurantId: Int = 0

    private lateinit var recyclerView: RecyclerView
    private lateinit var businessDaysBtn: Button
    private lateinit var menuBtn: Button
    private lateinit var reviewBtn: Button
    private lateinit var backBtn: Button
    private lateinit var favoriteBtn: ImageButton
    private lateinit var reviewOptionsLayout: LinearLayout
    private lateinit var reviewListBtn: Button
    private lateinit var reviewWriteBtn: Button

    private lateinit var businessDayAdapter: BusinessDayAdapter
    private lateinit var menuAdapter: MenuAdapter

    private lateinit var tokenManager: TokenManager

    private var isFavorite = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_detail)

        tokenManager = TokenManager(this)

        // 식당 ID 전달받기
        restaurantId = intent.getIntExtra("restaurantId", 2)

        // UI 초기화
        recyclerView = findViewById(R.id.businessDaysRecyclerView)
        businessDaysBtn = findViewById(R.id.businessDaysBtn)
        menuBtn = findViewById(R.id.menuBtn)
        reviewBtn = findViewById(R.id.reviewBtn)
        backBtn = findViewById(R.id.backBtn)
        favoriteBtn = findViewById(R.id.favoriteBtn)
        reviewOptionsLayout = findViewById(R.id.reviewOptionsLayout)
        reviewListBtn = findViewById(R.id.reviewListBtn)
        reviewWriteBtn = findViewById(R.id.reviewWriteBtn)

        // 리사이클러 뷰 설정
        recyclerView.layoutManager = LinearLayoutManager(this)
        businessDayAdapter = BusinessDayAdapter()
        menuAdapter = MenuAdapter()
        recyclerView.adapter = businessDayAdapter

        // 버튼 클릭 리스너 설정
        businessDaysBtn.setOnClickListener { showBusinessDays() }
        menuBtn.setOnClickListener { showMenuItems() }
        reviewBtn.setOnClickListener { showReviewOptions() }

        reviewListBtn.setOnClickListener {
            val intent = Intent(this, ReviewListActivity::class.java)
            startActivity(intent)
        }

        reviewWriteBtn.setOnClickListener {
            val intent = Intent(this, ReviewWriteActivity::class.java)
            startActivity(intent)
        }

        // 뒤로 가기 버튼 클릭
        backBtn.setOnClickListener { finish() }

        // 물리적 뒤로 가기 활성화
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })

        // 즐겨찾기 버튼 클릭
        favoriteBtn.setOnClickListener {
            toggleFavorite()
        }
    }



    // 더미 데이터 - 영업일
    private fun showBusinessDays() {
        recyclerView.adapter = businessDayAdapter
        reviewOptionsLayout.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE

        val dummyBusinessDays = listOf(
            BusinessDayInfo(1, "월요일", true, "12:00", "14:00", "09:00", "18:00", false),
            BusinessDayInfo(2, "화요일", false, "14:00", "15:00", "09:00", "18:00", false),
            BusinessDayInfo(3, "수요일", true, "13:00", "15:00", "09:00", "18:00", false)
        )

        businessDayAdapter.submitList(dummyBusinessDays)
    }

    // 더미 데이터 - 메뉴
    private fun showMenuItems() {
        recyclerView.adapter = menuAdapter
        reviewOptionsLayout.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE

        val dummyMenuItems = listOf(
            MenuResponse(1, 101, "치킨", 15000, "바삭한 후라이드 치킨", "https://example.com/chicken.jpg"),
            MenuResponse(2, 101, "피자", 18000, "고소한 치즈 피자", "https://example.com/pizza.jpg"),
            MenuResponse(3, 101, "스테이크", 35000, "프리미엄 비프 스테이크", "https://example.com/steak.jpg")
        )

        menuAdapter.submitList(dummyMenuItems)
    }

    // 리뷰 옵션 보이기
    private fun showReviewOptions() {
        reviewOptionsLayout.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }

    // 즐겨찾기 상태 토글
    private fun toggleFavorite() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val accessToken = tokenManager.getAccessToken() ?: ""

                // 토큰 확인 로그
                android.util.Log.d("AccessToken", "토큰 값: $accessToken")

                if (accessToken.isEmpty()) {
                    Toast.makeText(
                        this@RestaurantDetailActivity,
                        "로그인이 필요합니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                // API 호출
                val response = if (isFavorite) {
                    android.util.Log.d("API Request", "즐겨찾기 삭제 요청 시작")
                    RetrofitInstance.restaurantApi.deleteBookmark(
                        restaurantId,
                        "Bearer $accessToken"
                    )
                } else {
                    android.util.Log.d("API Request", "즐겨찾기 추가 요청 시작")
                    RetrofitInstance.restaurantApi.addBookmark(
                        restaurantId,
                        "Bearer $accessToken"
                    )
                }

                // 응답 확인 로그
                android.util.Log.d("API Response", "응답 코드: ${response.code()}, 본문: ${response.errorBody()?.string()}")

                when (response.code()) {
                    200 -> {
                        isFavorite = !isFavorite
                        updateFavoriteIcon()
                        val message = if (isFavorite) "즐겨찾기에 추가되었습니다." else "즐겨찾기에서 제거되었습니다."
                        Toast.makeText(this@RestaurantDetailActivity, message, Toast.LENGTH_SHORT).show()
                    }
                    400 -> {
                        val errorBody = response.errorBody()?.string()
                        android.util.Log.e("API Error", "잘못된 요청: $errorBody")
                        Toast.makeText(this@RestaurantDetailActivity, "잘못된 요청: $errorBody", Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        android.util.Log.e("API Error", "오류 발생: ${response.code()}")
                        Toast.makeText(
                            this@RestaurantDetailActivity,
                            "오류 발생: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("Network Error", "네트워크 오류: ${e.message}")
                Toast.makeText(
                    this@RestaurantDetailActivity,
                    "네트워크 오류: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // 즐겨찾기 아이콘 업데이트
    private fun updateFavoriteIcon() {
        favoriteBtn.setImageResource(
            if (isFavorite) R.drawable.favorite_icon else R.drawable.ic_bookmark
        )
    }
}
