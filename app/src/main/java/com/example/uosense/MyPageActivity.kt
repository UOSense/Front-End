package com.example.uosense

import TokenManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.uosense.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class MyPageActivity : AppCompatActivity() {

    // UI 요소 선언
    private lateinit var logOutBtn: Button
    private lateinit var backBtn: Button
    private lateinit var updateBtn: Button
    private lateinit var uploadImageBtn: Button
    private lateinit var removeImageBtn: Button
    private lateinit var favoriteDetailsBtn: Button
    private lateinit var reviewDetailsBtn: Button
    private lateinit var nicknameText: EditText
    private lateinit var profileImage: ImageView
    private lateinit var tokenManager: TokenManager

    private var uploadedImageUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_page)

        // 토큰 관리 객체 초기화
        tokenManager = TokenManager(this)

        // 로그인 여부 확인
        if (tokenManager.getRefreshToken().isNullOrEmpty()) {
            showToast("로그인이 필요합니다.")
            navigateToLoginActivity() // 로그인 화면으로 이동
            return
        }

        // UI 요소 초기화
        initializeUIElements()

        // 사용자 프로필 정보 가져오기
        fetchUserProfile()

        // 버튼 클릭 리스너 설정
        logOutBtn.setOnClickListener { logOutUser() }
        backBtn.setOnClickListener { navigateToMainActivity() }
        updateBtn.setOnClickListener { updateUserProfile() }
        uploadImageBtn.setOnClickListener { pickImageFromGallery() }
        removeImageBtn.setOnClickListener { removeProfileImage() }

        favoriteDetailsBtn.setOnClickListener {
            startActivity(Intent(this, FavoriteListActivity::class.java)) // 즐겨찾기 화면으로 이동
        }

        reviewDetailsBtn.setOnClickListener {
            startActivity(Intent(this, MyReviewListActivity::class.java)) // 리뷰 목록 화면으로 이동
        }

        // 뒤로 가기 버튼 동작 설정
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateToMainActivity() // 메인 화면으로 이동
            }
        })
    }

    // UI 요소 초기화 메서드
    private fun initializeUIElements() {
        logOutBtn = findViewById(R.id.logOutBtn)
        backBtn = findViewById(R.id.backBtn)
        updateBtn = findViewById(R.id.editProfile)
        uploadImageBtn = findViewById(R.id.uploadImageBtn)
        removeImageBtn = findViewById(R.id.removeImageBtn)
        favoriteDetailsBtn = findViewById(R.id.favoriteDetailsBtn)
        reviewDetailsBtn = findViewById(R.id.reviewDetailsBtn)
        nicknameText = findViewById(R.id.userName)
        profileImage = findViewById(R.id.profileImage)
    }

    // 사용자 프로필 정보 가져오기
    private fun fetchUserProfile() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val accessToken = tokenManager.ensureValidAccessToken() ?: run {
                    showToast("로그인이 필요합니다.")
                    navigateToLoginActivity()
                    return@launch
                }

                // API 호출하여 사용자 프로필 가져오기
                val response = RetrofitInstance.restaurantApi.getUserProfile("Bearer $accessToken")
                nicknameText.setText(response.nickname)
                uploadedImageUrl = response.imageUrl

                // 프로필 이미지 로딩
                Glide.with(this@MyPageActivity)
                    .load(response.imageUrl)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .into(profileImage)

            } catch (e: Exception) {
                showToast("프로필 조회 실패: ${e.message}") // 오류 메시지 출력
            }
        }
    }

    // 사용자 프로필 업데이트 메서드
    private fun updateUserProfile() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val accessToken = tokenManager.ensureValidAccessToken() ?: run {
                    showToast("로그인이 필요합니다.")
                    navigateToLoginActivity()
                    return@launch
                }

                val newNickname = nicknameText.text.toString().takeIf { it.isNotEmpty() }
                val imagePart = uploadedImageUrl?.let { createMultipartBodyFromUri(it) }

                // API 호출하여 프로필 업데이트
                val response = RetrofitInstance.restaurantApi.updateUserProfile(
                    accessToken = "Bearer $accessToken",
                    nickname = newNickname,
                    image = imagePart
                )

                if (response.isSuccessful) {
                    showToast("프로필이 성공적으로 업데이트되었습니다.")
                    fetchUserProfile() // 업데이트된 정보 다시 가져오기
                } else {
                    showToast("업데이트 실패: ${response.code()}") // 오류 메시지 출력
                }
            } catch (e: Exception) {
                showToast("업데이트 실패: ${e.message}") // 예외 처리
            }
        }
    }

    // 갤러리에서 이미지 선택
    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
        startActivityForResult(intent, 1000)
    }

    // 선택된 이미지 처리
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000 && resultCode == RESULT_OK) {
            val imageUri = data?.data
            uploadedImageUrl = imageUri.toString()
            Glide.with(this).load(imageUri).placeholder(R.drawable.ic_profile_placeholder).into(profileImage)
        }
    }

    // 프로필 이미지 제거
    private fun removeProfileImage() {
        uploadedImageUrl = null
        profileImage.setImageResource(R.drawable.ic_profile_placeholder)
        showToast("이미지가 제거되었습니다.") // 메시지 출력
    }

    // 메인 화면으로 이동
    private fun navigateToMainActivity() {
        val intent = Intent(this, RestaurantDetailActivity::class.java).apply {
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

    // 로그아웃 처리
    private fun logOutUser() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val refreshToken = tokenManager.getRefreshToken() ?: run {
                    showToast("로그인 정보가 없습니다.")
                    navigateToLoginActivity()
                    return@launch
                }

                val response = RetrofitInstance.restaurantApi.logoutUser("refresh=$refreshToken")

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        showToast("로그아웃 성공!")
                        tokenManager.clearTokens() // 토큰 초기화
                        navigateToLoginActivity() // 로그인 화면으로 이동
                    } else {
                        showToast("로그아웃 실패: ${response.code()}") // 오류 메시지 출력
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("서버 오류 발생: ${e.message}") // 예외 처리
                }
            }
        }
    }

    // 메시지 출력 메서드
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // URI에서 MultipartBody 생성
    private fun createMultipartBodyFromUri(uri: String?): MultipartBody.Part? {
        if (uri == null) return null

        return try {
            val fileUri = Uri.parse(uri)
            val file = File(fileUri.path!!)
            val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("image", file.name, requestBody)
        } catch (e: Exception) {
            Log.e("MultipartError", "파일 생성 실패: ${e.message}")
            null
        }
    }
}
