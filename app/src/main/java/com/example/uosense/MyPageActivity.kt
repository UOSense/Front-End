package com.example.uosense

import TokenManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.uosense.models.UpdateRequest
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

        tokenManager = TokenManager(this)

        // 리프레시 토큰 검증
        val refreshToken = tokenManager.getRefreshToken()
        if (refreshToken.isNullOrEmpty()) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            navigateToLoginActivity()
            return
        }

        // UI 요소 초기화
        logOutBtn = findViewById(R.id.logOutBtn)
        backBtn = findViewById(R.id.backBtn)
        updateBtn = findViewById(R.id.editProfile)
        uploadImageBtn = findViewById(R.id.uploadImageBtn)
        removeImageBtn = findViewById(R.id.removeImageBtn)
        favoriteDetailsBtn = findViewById(R.id.favoriteDetailsBtn)
        reviewDetailsBtn = findViewById(R.id.reviewDetailsBtn)
        nicknameText = findViewById(R.id.userName)
        profileImage = findViewById(R.id.profileImage)

        fetchUserProfile()

        logOutBtn.setOnClickListener { logOutUser() }
        backBtn.setOnClickListener { navigateToMainActivity() }
        updateBtn.setOnClickListener { updateUserProfile() }
        uploadImageBtn.setOnClickListener { pickImageFromGallery() }
        removeImageBtn.setOnClickListener { removeProfileImage() }

        favoriteDetailsBtn.setOnClickListener {
            startActivity(Intent(this, FavoriteListActivity::class.java))
        }

        reviewDetailsBtn.setOnClickListener {
            startActivity(Intent(this, MyReviewListActivity::class.java))
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateToMainActivity()
            }
        })
    }

    // 프로필 조회
    private fun fetchUserProfile() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val accessToken = tokenManager.getAccessToken().orEmpty()
                if (accessToken.isEmpty()) {
                    Toast.makeText(this@MyPageActivity, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                    navigateToLoginActivity()
                    return@launch
                }

                val response = RetrofitInstance.restaurantApi.getUserProfile("Bearer $accessToken")
                nicknameText.setText(response.nickname)
                uploadedImageUrl = response.imageUrl

                Glide.with(this@MyPageActivity)
                    .load(response.imageUrl)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .into(profileImage)

            } catch (e: Exception) {
                Toast.makeText(this@MyPageActivity, "프로필 조회 실패: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    // 프로필 업데이트
    private fun updateUserProfile() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val accessToken = tokenManager.getAccessToken().orEmpty()
                if (accessToken.isEmpty()) {
                    Toast.makeText(this@MyPageActivity, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
                    navigateToLoginActivity()
                    return@launch
                }

                // 닉네임을 비워서 전달하거나 수정하지 않으려면 null로 설정
                val newNickname = nicknameText.text.toString().takeIf { it.isNotEmpty() }

                // 이미지 수정하지 않을 경우 null
                val imagePart = uploadedImageUrl?.let { createMultipartBodyFromUri(it) }

                Log.d("UpdateRequestLog", "닉네임: $newNickname")
                Log.d("UpdateRequestLog", "이미지 존재 여부: ${imagePart != null}")

                val response = RetrofitInstance.restaurantApi.updateUserProfile(
                    accessToken = "Bearer $accessToken",
                    nickname = newNickname, // null 전달 가능
                    image = imagePart // null 전달 가능
                )

                if (response.isSuccessful) {
                    Toast.makeText(this@MyPageActivity, "프로필이 성공적으로 업데이트되었습니다.", Toast.LENGTH_SHORT)
                        .show()
                    fetchUserProfile() // 업데이트 후 새로고침
                } else {
                    Log.e(
                        "UpdateRequestError",
                        "업데이트 실패: ${response.code()} - ${response.errorBody()?.string()}"
                    )
                    Toast.makeText(
                        this@MyPageActivity,
                        "업데이트 실패: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("UpdateRequestException", "예외 발생: ${e.message}")
                Toast.makeText(this@MyPageActivity, "업데이트 실패: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }


    // 이미지 선택
    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 1000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000 && resultCode == RESULT_OK) {
            val imageUri = data?.data
            uploadedImageUrl = imageUri.toString()

            Glide.with(this)
                .load(imageUri)
                .placeholder(R.drawable.ic_profile_placeholder)
                .into(profileImage)
        }
    }

    // 이미지 제거
    private fun removeProfileImage() {
        uploadedImageUrl = null
        profileImage.setImageResource(R.drawable.ic_profile_placeholder)
        Toast.makeText(this, "이미지가 제거되었습니다.", Toast.LENGTH_SHORT).show()
    }

    // 네비게이션 메소드들
    private fun navigateToMainActivity() {
        val intent = Intent(this, RestaurantDetailActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun navigateToLoginActivity() {
        val intent = Intent(this, StartActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun logOutUser() {
        val refreshToken = tokenManager.getRefreshToken()

        if (refreshToken.isNullOrEmpty()) {
            Toast.makeText(this, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show()
            navigateToLoginActivity()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.restaurantApi.logoutUser("refresh=$refreshToken")

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@MyPageActivity, "로그아웃 성공!", Toast.LENGTH_SHORT).show()
                        tokenManager.clearTokens()
                        navigateToLoginActivity()
                    } else {
                        Toast.makeText(
                            this@MyPageActivity,
                            "로그아웃 실패: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MyPageActivity,
                        "서버 오류 발생: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun createMultipartBodyFromUri(uri: String?): MultipartBody.Part? {
        if (uri == null) return null

        return try {
            val contentResolver = contentResolver
            val fileUri = Uri.parse(uri)

            // 파일 이름 추출
            val fileName = contentResolver.query(fileUri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                cursor.getString(nameIndex)
            } ?: "file.jpg"

            // InputStream에서 데이터를 읽고 MultipartBody.Part로 변환
            val inputStream = contentResolver.openInputStream(fileUri)
            val tempFile = File.createTempFile("upload", fileName, cacheDir).apply {
                outputStream().use { inputStream?.copyTo(it) }
            }

            val requestBody = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("image", fileName, requestBody)
        } catch (e: Exception) {
            Log.e("MultipartError", "파일 생성 실패: ${e.message}")
            null
        }
    }
}
