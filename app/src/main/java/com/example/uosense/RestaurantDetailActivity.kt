package com.example.uosense

import BusinessDayAdapter
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.uosense.adapters.MenuAdapter
import com.example.uosense.databinding.ActivityControlMainBinding
import com.example.uosense.databinding.ActivityRestaurantDetailBinding
import com.example.uosense.databinding.ActivitySelectedDoorBinding
import com.example.uosense.models.BusinessDayInfo
import com.example.uosense.models.MenuResponse
import com.example.uosense.models.RestaurantInfo
import com.example.uosense.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    private var restaurantId: Int = -1

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
            startActivity(Intent(this, ReviewListActivity::class.java).apply {
                putExtra("restaurantId", restaurantId)
            })
        }

        reviewWriteBtn.setOnClickListener {
            startActivity(Intent(this, ReviewWriteActivity::class.java).apply {
                putExtra("restaurantId", restaurantId)
            })
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

        // 식당 ID 수신
        restaurantId = intent.getIntExtra("restaurantId", -1)
        if (restaurantId == -1) {
            showToast("식당 정보를 불러올 수 없습니다.")
            finish()
            return
        }

        // 데이터 로드
        loadRestaurantData()
        loadBusinessDays()
        loadRestaurantImages()
    }

    // 식당 데이터 로드
    private fun loadRestaurantData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.restaurantApi.getRestaurantById(restaurantId)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        bindRestaurantData(response.body()!!)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 식당 정보 바인딩
    private fun bindRestaurantData(restaurant: RestaurantInfo) {
        findViewById<TextView>(R.id.restaurantName).text = restaurant.name
        findViewById<TextView>(R.id.restaurantCategory).text = restaurant.category ?: "정보 없음"
        findViewById<TextView>(R.id.restaurantDescription).text = restaurant.description ?: "정보 없음"
        findViewById<TextView>(R.id.restaurantAddress).text = "주소: ${restaurant.address}"
        findViewById<TextView>(R.id.restaurantPhoneNumber).text = "전화: ${restaurant.phoneNumber ?: "정보 없음"}"
        findViewById<TextView>(R.id.restaurantRating).text = "평점: ${restaurant.rating ?: "정보 없음"}"
        findViewById<TextView>(R.id.restaurantReview).text = restaurant.reviewCount?.toString() ?: "0"

        isFavorite = restaurant.bookmarkId != null
        updateFavoriteIcon()
    }

    // 영업일 데이터 로드
    private fun loadBusinessDays() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.restaurantApi.getBusinessDayList(restaurantId)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        businessDayAdapter.submitList(response.body()!!.businessDayInfoList)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 메뉴 데이터 로드
    private fun loadMenuItems() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.restaurantApi.getMenu(restaurantId)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        menuAdapter.submitList(response.body()!!)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 식당 이미지 로드
    private fun loadRestaurantImages() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.restaurantApi.getRestaurantImages(restaurantId)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val images = response.body()!!.imageList.map { it.url }
                        // 이미지 리사이클러뷰에 추가
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    // 영업일 표시
    private fun showBusinessDays() {
        recyclerView.adapter = businessDayAdapter
        reviewOptionsLayout.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
        loadBusinessDays() // 실제 영업일 데이터를 로드
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

    // 리뷰 옵션 보이기
    private fun showReviewOptions() {
        reviewOptionsLayout.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }

    // 리뷰 목록 표시
    private fun showMenuItems() {
        recyclerView.adapter = menuAdapter
        reviewOptionsLayout.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
        loadMenuItems() // 실제 메뉴 데이터 로드
    }

    // 리뷰 목록 표시
    private fun showReviewList() {
        startActivity(Intent(this, ReviewListActivity::class.java).apply {
            putExtra("restaurantId", restaurantId)
        })
    }

    // 토스트 메시지
    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}
