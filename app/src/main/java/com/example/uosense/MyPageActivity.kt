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

        if (tokenManager.getRefreshToken().isNullOrEmpty()) {
            showToast("로그인이 필요합니다.")
            navigateToLoginActivity()
            return
        }

        initializeUIElements()
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

    private fun fetchUserProfile() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val accessToken = tokenManager.ensureValidAccessToken() ?: run {
                    showToast("로그인이 필요합니다.")
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
                showToast("프로필 조회 실패: ${e.message}")
            }
        }
    }

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

                val response = RetrofitInstance.restaurantApi.updateUserProfile(
                    accessToken = "Bearer $accessToken",
                    nickname = newNickname,
                    image = imagePart
                )

                if (response.isSuccessful) {
                    showToast("프로필이 성공적으로 업데이트되었습니다.")
                    fetchUserProfile()
                } else {
                    showToast("업데이트 실패: ${response.code()}")
                }
            } catch (e: Exception) {
                showToast("업데이트 실패: ${e.message}")
            }
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
        startActivityForResult(intent, 1000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000 && resultCode == RESULT_OK) {
            val imageUri = data?.data
            uploadedImageUrl = imageUri.toString()
            Glide.with(this).load(imageUri).placeholder(R.drawable.ic_profile_placeholder).into(profileImage)
        }
    }

    private fun removeProfileImage() {
        uploadedImageUrl = null
        profileImage.setImageResource(R.drawable.ic_profile_placeholder)
        showToast("이미지가 제거되었습니다.")
    }

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
                        tokenManager.clearTokens()
                        navigateToLoginActivity()
                    } else {
                        showToast("로그아웃 실패: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("서버 오류 발생: ${e.message}")
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

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
