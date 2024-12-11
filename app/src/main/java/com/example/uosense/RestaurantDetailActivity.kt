package com.example.uosense

import BusinessDayAdapter
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

class RestaurantDetailActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var businessDaysBtn: Button
    private lateinit var menuBtn: Button
    private lateinit var reviewBtn: Button
    private lateinit var backBtn: Button
    private lateinit var favoriteButton: ImageButton
    private lateinit var reviewOptionsLayout: LinearLayout
    private lateinit var reviewListBtn: Button
    private lateinit var reviewWriteBtn: Button

    private lateinit var businessDayAdapter: BusinessDayAdapter
    private lateinit var menuAdapter: MenuAdapter

    private var isFavorite = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_detail)

        // UI 초기화
        recyclerView = findViewById(R.id.businessDaysRecyclerView)
        businessDaysBtn = findViewById(R.id.businessDaysBtn)
        menuBtn = findViewById(R.id.menuBtn)
        reviewBtn = findViewById(R.id.reviewBtn)
        backBtn = findViewById(R.id.backBtn)
        favoriteButton = findViewById(R.id.favoriteButton)
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
            val intent = Intent(this, StartActivity::class.java)
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
        favoriteButton.setOnClickListener { toggleFavorite() }
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
        isFavorite = !isFavorite
        val message = if (isFavorite) "즐겨찾기에 추가됨" else "즐겨찾기에서 제거됨"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        updateFavoriteIcon()
    }

    // 즐겨찾기 아이콘 업데이트
    private fun updateFavoriteIcon() {
        favoriteButton.setImageResource(
            if (isFavorite) R.drawable.favorite_icon else R.drawable.ic_bookmark
        )
    }
}
