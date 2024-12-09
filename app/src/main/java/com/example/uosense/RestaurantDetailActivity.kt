package com.example.uosense

import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.uosense.adapters.BusinessDayAdapter
import com.example.uosense.models.BusinessDay
import com.example.uosense.models.RestaurantInfo
import com.example.uosense.viewmodel.RestaurantViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RestaurantDetailActivity : AppCompatActivity() {

    private val viewModel: RestaurantViewModel by viewModels()
    private lateinit var favoriteButton: ImageButton
    private lateinit var businessDaysAdapter: BusinessDayAdapter

    private var restaurantId: Int = -1
    private var isFavorite = false
    private var bookmarkId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_UOSense)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_detail)

        initializeViews()
        observeViewModel()

        restaurantId = intent.getIntExtra("restaurantId", -1)
        if (restaurantId != -1) {
            viewModel.fetchRestaurantById(restaurantId)
            Log.d("RESTAURANT_DETAIL", "받은 식당 ID: $restaurantId")
        } else {
            AppUtils.showToast(this,"식당 정보를 불러오지 못했습니다.")
        }

    }

    private fun initializeViews() {
        favoriteButton = findViewById(R.id.favoriteButton)
        favoriteButton.setOnClickListener { handleFavoriteButtonClick() }

        val businessDaysRecyclerView = findViewById<RecyclerView>(R.id.businessDaysRecyclerView)
        businessDaysRecyclerView.layoutManager = LinearLayoutManager(this)
        businessDaysAdapter = BusinessDayAdapter()
        businessDaysRecyclerView.adapter = businessDaysAdapter
    }

    private fun observeViewModel() {
        viewModel.restaurantInfo.observe(this) { restaurantInfo ->
            if (restaurantInfo != null) {
                updateUI(restaurantInfo)
                bookmarkId = restaurantInfo.bookmarkId
                isFavorite = bookmarkId != null
                updateFavoriteButtonIcon()
            } else {
                AppUtils.showToast(this,"식당 정보를 불러오지 못했습니다.")
            }
        }

        viewModel.isBookmarked.observe(this) { bookmarked ->
            isFavorite = bookmarked
            updateFavoriteButtonIcon()
        }
    }

    private fun handleFavoriteButtonClick() {
        if (isFavorite) {
            bookmarkId?.let { viewModel.deleteBookmark(it) }
        } else {
            viewModel.addBookmark(restaurantId)
        }
    }

    /** 서버 연동 시
     * private fun handleFavoriteButtonClick() {
     *     lifecycleScope.launch {
     *         try {
     *             if (isFavorite && bookmarkId != null) {
     *                 val response = viewModel.deleteBookmark(bookmarkId!!)
     *                 if (response.isSuccessful) {
     *                     isFavorite = false
     *                     bookmarkId = null
     *                     showToast("즐겨찾기에서 삭제되었습니다.")
     *                 } else {
     *                     showToast("즐겨찾기 삭제 실패")
     *                 }
     *             } else {
     *                 val response = viewModel.addBookmark(restaurantId)
     *                 if (response.isSuccessful) {
     *                     isFavorite = true
     *                     bookmarkId = restaurantId // 가정: 서버에서 식별 가능한 ID 반환
     *                     showToast("즐겨찾기에 추가되었습니다.")
     *                 } else {
     *                     showToast("즐겨찾기 추가 실패")
     *                 }
     *             }
     *             updateFavoriteButtonIcon()
     *         } catch (e: Exception) {
     *             showToast("서버 오류 발생")
     *             e.printStackTrace()
     *         }
     *     }
     * }
     *
     */

    private fun updateUI(restaurantInfo: RestaurantInfo) {
        findViewById<TextView>(R.id.restaurantName).text = restaurantInfo.name
        findViewById<TextView>(R.id.restaurantAddress).text = restaurantInfo.address
        findViewById<TextView>(R.id.restaurantCategory).text =
            restaurantInfo.category ?: "카테고리 정보 없음"
        findViewById<TextView>(R.id.restaurantDescription).text =
            restaurantInfo.description ?: "설명 없음"
        findViewById<TextView>(R.id.restaurantRating).text =
            "평점: ${restaurantInfo.rating ?: "정보 없음"}"
        findViewById<TextView>(R.id.restaurantPhoneNumber).text =
            "전화번호: ${restaurantInfo.phoneNumber ?: "전화번호 정보 없음"}"

        businessDaysAdapter.submitList(restaurantInfo.businessDays ?: emptyList())
    }

    private fun updateFavoriteButtonIcon() {
        favoriteButton.setImageResource(
            if (isFavorite) R.drawable.favorite_icon else R.drawable.ic_bookmark
        )
    }


    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
