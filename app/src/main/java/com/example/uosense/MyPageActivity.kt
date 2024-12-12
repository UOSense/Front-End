package com.example.uosense

import TokenManager
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.uosense.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyPageActivity : AppCompatActivity() {

    // 버튼 및 토큰 관리자 선언
    private lateinit var logOutBtn: Button
    private lateinit var backBtn: Button
    private lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_page)

        // TokenManager 초기화
        tokenManager = TokenManager(this)

        // 리프레시 토큰 검증
        val refreshToken = tokenManager.getRefreshToken()
        if (refreshToken.isNullOrEmpty()) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            navigateToLoginActivity() // 로그인 화면으로 이동
            return
        }

        // 로그아웃 버튼 초기화
        logOutBtn = findViewById(R.id.logOutBtn)
        backBtn = findViewById(R.id.backBtn)

        // 로그아웃 버튼 클릭 이벤트
        logOutBtn.setOnClickListener {
            logOutUser()
        }

        // 뒤로 가기 버튼 클릭 이벤트
        backBtn.setOnClickListener {
            navigateToMainActivity()
        }

        // 물리적 뒤로 가기 처리
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateToMainActivity()
            }
        })

        // 전체보기 버튼 클릭 리스너
        val favoriteDetails: TextView = findViewById(R.id.favoriteDetails)
        favoriteDetails.setOnClickListener {
            val intent = Intent(this, FavoriteListActivity::class.java)
            startActivity(intent)
        }
    }

    // 로그아웃 처리 함수
    private fun logOutUser() {
        val refreshToken = tokenManager.getRefreshToken()

        if (refreshToken.isNullOrEmpty()) {
            Toast.makeText(this, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            navigateToLoginActivity()
            return
        }

        // 비동기 API 호출
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.restaurantApi.logoutUser("refresh=$refreshToken")

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@MyPageActivity, "로그아웃 성공!", Toast.LENGTH_SHORT).show()
                        tokenManager.clearTokens()
                        navigateToLoginActivity()
                    } else {
                        Toast.makeText(this@MyPageActivity, "로그아웃 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MyPageActivity, "서버 오류 발생: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 메인 액티비티로 이동
    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    // 로그인 화면으로 이동
    private fun navigateToLoginActivity() {
        val intent = Intent(this, StartActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}
