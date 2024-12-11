// 필요한 import 추가
package com.example.uosense

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.uosense.adapters.BusinessDayAdapter
import com.example.uosense.adapters.ImageAdapter
import com.example.uosense.adapters.MenuAdapter
import com.example.uosense.models.*
import com.example.uosense.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RestaurantDetailActivity : AppCompatActivity() {

    private lateinit var businessDayAdapter: BusinessDayAdapter
    private lateinit var menuAdapter: MenuAdapter
    private lateinit var imageAdapter: ImageAdapter

    private lateinit var recyclerView: RecyclerView
    private lateinit var imageRecyclerView: RecyclerView

    private lateinit var businessDaysBtn: Button
    private lateinit var menuBtn: Button
    private lateinit var reviewBtn: Button
    private lateinit var backBtn: Button
    private lateinit var favoriteButton: ImageButton

    private lateinit var reviewOptionsLayout: LinearLayout
    private lateinit var reviewListBtn: Button
    private lateinit var reviewWriteBtn: Button

    private var isFavorite = false
    private var restaurantId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_detail)

        initUI()

        // 식당 ID 수신
        restaurantId = intent.getIntExtra("restaurantId", -1)
        if (restaurantId == -1) {
            showToast("식당 정보를 불러올 수 없습니다.")
            finish()
            return
        }

        loadRestaurantData()
        loadBusinessDays()
        loadRestaurantImages()
    }

    private fun initUI() {
        recyclerView = findViewById(R.id.businessDaysRecyclerView)
        imageRecyclerView = findViewById(R.id.restaurantImageRecyclerView)

        businessDaysBtn = findViewById(R.id.businessDaysBtn)
        menuBtn = findViewById(R.id.menuBtn)
        reviewBtn = findViewById(R.id.reviewBtn)
        backBtn = findViewById(R.id.backBtn)
        favoriteButton = findViewById(R.id.favoriteButton)

        reviewOptionsLayout = findViewById(R.id.reviewOptionsLayout)
        reviewListBtn = findViewById(R.id.reviewListBtn)
        reviewWriteBtn = findViewById(R.id.reviewWriteBtn)

        businessDayAdapter = BusinessDayAdapter()
        menuAdapter = MenuAdapter()
        imageAdapter = ImageAdapter()

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = businessDayAdapter

        imageRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        imageRecyclerView.adapter = imageAdapter

        backBtn.setOnClickListener { finish() }
        favoriteButton.setOnClickListener { toggleFavorite() }

        businessDaysBtn.setOnClickListener { showBusinessDays() }
        menuBtn.setOnClickListener {
            loadMenuItems()
            showMenuItems() }
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
    }

    private fun loadRestaurantData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.restaurantApi.getRestaurantById(restaurantId)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        bindRestaurantData(response.body()!!)
                    } else {
                        showToast("식당 정보를 불러올 수 없습니다.")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showToast("식당 정보를 불러오는 중 오류 발생")
            }
        }
    }

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

    private fun loadBusinessDays() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.restaurantApi.getBusinessDayList(restaurantId)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        businessDayAdapter.submitList(response.body()!!.businessDayInfoList)
                    } else {
                        showToast("영업일 정보를 불러올 수 없습니다.")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showToast("영업일 정보를 불러오는 중 오류 발생")
            }
        }
    }

    private fun loadRestaurantImages() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.restaurantApi.getRestaurantImages(restaurantId)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val images = response.body()!!.imageList.map { it.url }
                        imageAdapter.submitList(images)
                    } else {
                        showToast("이미지를 불러올 수 없습니다.")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showToast("이미지를 불러오는 중 오류 발생")
            }
        }
    }

    private fun toggleFavorite() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (isFavorite) {
                    RetrofitInstance.restaurantApi.deleteBookmark(restaurantId)
                } else {
                    RetrofitInstance.restaurantApi.addBookmark(restaurantId)
                }
                isFavorite = !isFavorite
                withContext(Dispatchers.Main) {
                    updateFavoriteIcon()
                    showToast(if (isFavorite) "즐겨찾기에 추가됨" else "즐겨찾기에서 제거됨")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showToast("즐겨찾기 업데이트 실패")
            }
        }
    }

    private fun showBusinessDays() {
        recyclerView.adapter = businessDayAdapter
        reviewOptionsLayout.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }

    private fun showMenuItems() {
        recyclerView.adapter = menuAdapter
        reviewOptionsLayout.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }

    private fun showReviewOptions() {
        reviewOptionsLayout.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
    }

    private fun updateFavoriteIcon() {
        favoriteButton.setImageResource(
            if (isFavorite) R.drawable.favorite_icon else R.drawable.ic_bookmark
        )
    }
    private fun loadMenuItems() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.restaurantApi.getMenu(restaurantId)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        menuAdapter.submitList(response.body()!!)
                    } else {
                        showToast("메뉴 정보를 불러올 수 없습니다.")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showToast("메뉴 로딩 중 오류 발생")
            }
        }
    }


    // 기존 showToast 메서드 수정
    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}
