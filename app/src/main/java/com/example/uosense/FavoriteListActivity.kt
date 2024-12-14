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
import com.example.uosense.adapters.FavoriteAdapter
import com.example.uosense.models.BookMarkResponse
import com.example.uosense.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FavoriteListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var favoriteAdapter: FavoriteAdapter
    private lateinit var tokenManager: TokenManager
    private lateinit var backBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite_list)

        tokenManager = TokenManager(this)

        backBtn = findViewById(R.id.backBtn)
        backBtn.setOnClickListener {
            navigateToMyPage()
        }

        setupRecyclerView()
        fetchFavorites()
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.favoriteRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        favoriteAdapter = FavoriteAdapter(emptyList())
        recyclerView.adapter = favoriteAdapter
    }

    private fun fetchFavorites() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val accessToken = tokenManager.getAccessToken().orEmpty()
                if (accessToken.isEmpty()) {
                    showToast("로그인이 필요합니다.")
                    return@launch
                }

                val favorites = RetrofitInstance.restaurantApi.getMyBookmarks("Bearer $accessToken")
                if (favorites.isNotEmpty()) {
                    favoriteAdapter.updateData(favorites)
                    recyclerView.visibility = View.VISIBLE
                } else {
                    showToast("즐겨찾기 목록이 비어 있습니다.")
                }
            } catch (e: Exception) {
                showToast("오류 발생: ${e.message}")
            }
        }
    }

    private fun navigateToMyPage() {
        val intent = Intent(this, MyPageActivity::class.java)
        startActivity(intent)
        finish() // 현재 액티비티 종료
    }

    private fun showToast(message: String) {
        Toast.makeText(this@FavoriteListActivity, message, Toast.LENGTH_SHORT).show()
    }
}
