package com.example.uosense

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.uosense.adapters.GenericAdapter

class RestaurantDetailActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var newsBtn: Button
    private lateinit var menuBtn: Button
    private lateinit var reviewBtn: Button
    private lateinit var backBtn: Button
    private lateinit var favoriteButton: ImageButton

    private lateinit var adapter: GenericAdapter

    private var isFavorite = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_detail)

        // UI 초기화
        recyclerView = findViewById(R.id.businessDaysRecyclerView)
        newsBtn = findViewById(R.id.newsBtn)
        menuBtn = findViewById(R.id.menuBtn)
        reviewBtn = findViewById(R.id.reviewBtn)
        backBtn = findViewById(R.id.backBtn)
        favoriteButton = findViewById(R.id.favoriteButton)

        // 리사이클러 뷰 설정
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = GenericAdapter()
        recyclerView.adapter = adapter

        // 버튼 클릭 리스너 설정
        newsBtn.setOnClickListener { showList("소식") }
        menuBtn.setOnClickListener { showList("메뉴") }
        reviewBtn.setOnClickListener { showList("리뷰") }

        // 뒤로 가기 버튼 클릭
        backBtn.setOnClickListener {
            finish()  // 현재 액티비티 종료
        }

        // 즐겨찾기 버튼 클릭
        favoriteButton.setOnClickListener {
            toggleFavorite()
        }

        // 초기 데이터 로드
        loadRestaurantData()
    }

    // 버튼 클릭 시 데이터 변경
    private fun showList(section: String) {
        val items = List(10) { "$section 아이템 $it" }
        adapter.submitList(items)
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

    // 식당 정보 로딩 (가상 데이터)
    private fun loadRestaurantData() {
        findViewById<TextView>(R.id.restaurantName).text = "실크로드"
        findViewById<TextView>(R.id.restaurantCategory).text = "바(Bar)"
        findViewById<TextView>(R.id.restaurantDescription).text = "아지트 시립대 앞 분위기 좋은 칵테일바"
        findViewById<TextView>(R.id.restaurantAddress).text = "주소: 서울 동대문구 전농로 219"
        findViewById<TextView>(R.id.restaurantPhoneNumber).text = "전화: 02-2244-2229"
        findViewById<TextView>(R.id.restaurantRating).text = "평점: 4.65"
    }
}
