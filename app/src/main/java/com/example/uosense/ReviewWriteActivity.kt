package com.example.uosense

import TokenManager
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import androidx.core.content.ContextCompat
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.uosense.models.ReviewRequest
import com.example.uosense.network.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class ReviewWriteActivity : AppCompatActivity() {


    private lateinit var tokenManager: TokenManager
    // 후기 입력 관련
    private lateinit var reviewInput: EditText
    private lateinit var reviewCharacterCount: TextView

    // 사진 추가 관련
    private lateinit var photoAttachmentLayout: LinearLayout
    private val PICK_IMAGE_REQUEST = 1

    // 별점 저장 변수
    private lateinit var myRatingBar: RatingBar
    private var currentRating: Float = 0f

    // 특징 선택 상태 변수
    private var selectedTag: String? = null

    // 리뷰 이벤트 체크박스
    private lateinit var reviewEventCheckBox: CheckBox
    private var reviewEventCheck = false

    // 등록 버튼
    private lateinit var registerReviewBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review_write)

        // TokenManager 초기화
        tokenManager = TokenManager(this)
        // 초기화
        reviewInput = findViewById(R.id.reviewInput)
        reviewCharacterCount = findViewById(R.id.reviewCharacterCount)
        photoAttachmentLayout = findViewById(R.id.photoAttachmentLayout)
        myRatingBar = findViewById(R.id.myRatingBar)
        reviewEventCheckBox = findViewById(R.id.eventCheckBox)
        registerReviewBtn = findViewById(R.id.registerReviewBtn)

        // 사진 추가 버튼 설정
        val photoPlusBtn: Button = findViewById(R.id.photoPlusBtn)
        photoPlusBtn.setOnClickListener { openImagePicker() }

        // 리뷰 이벤트 체크박스 상태 업데이트
        reviewEventCheckBox.setOnCheckedChangeListener { _, isChecked ->
            reviewEventCheck = isChecked
        }

        // 별점 조정
        myRatingBar.setIsIndicator(false)
        myRatingBar.setOnRatingBarChangeListener { _, rating, _ ->
            currentRating = rating
            Toast.makeText(this, "현재 별점: $currentRating", Toast.LENGTH_SHORT).show()
        }

        // 후기 입력 시 글자 수 업데이트
        reviewInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val currentLength = s?.length ?: 0
                reviewCharacterCount.text = "$currentLength/200"
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // 특징 버튼 설정
        setupFeatureButton(R.id.serviceBtn, "GOOD_SERVICE", R.color.green)
        setupFeatureButton(R.id.dateRecommendBtn, "DATE_PLACE", R.color.pink)
        setupFeatureButton(R.id.soloEatBtn, "SOLO_POSSIBLE", R.color.teal_700)
        setupFeatureButton(R.id.kindOwnerBtn, "KIND_BOSS", R.color.purple_200)
        setupFeatureButton(R.id.interiorBtn, "NICE_INTERIOR", R.color.orange)

        // 등록 버튼 클릭 리스너 설정
        registerReviewBtn.setOnClickListener { submitReview() }
    }

    // 사진 선택기 열기
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    // 이미지 추가 기능
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            val imageUri: Uri? = data.data
            addImageToLayout(imageUri)
        }
    }

    private fun addImageToLayout(imageUri: Uri?) {
        val imageView = ImageView(this)
        val layoutParams = LinearLayout.LayoutParams(200, 200).apply {
            setMargins(8, 8, 8, 8)
        }
        imageView.layoutParams = layoutParams
        imageView.setImageURI(imageUri)
        photoAttachmentLayout.addView(imageView, 0)
        Toast.makeText(this, "이미지가 추가되었습니다.", Toast.LENGTH_SHORT).show()
    }

    // 특징 버튼 설정 함수
    private fun setupFeatureButton(buttonId: Int, tag: String, colorId: Int) {
        val button: Button = findViewById(buttonId)
        val defaultBackground: Drawable = button.background

        button.setOnClickListener {
            // 이전에 선택된 버튼 초기화
            if (selectedTag == tag) {
                selectedTag = null
                button.background = defaultBackground
            } else {
                // 모든 버튼 초기화
                resetFeatureButtons()
                // 새로운 버튼 활성화
                selectedTag = tag
                button.setBackgroundColor(ContextCompat.getColor(this, colorId))
            }
        }
    }

    // 모든 버튼 상태 초기화 함수
    private fun resetFeatureButtons() {
        val buttonBackgrounds = mapOf(
            R.id.serviceBtn to R.drawable.rounded_border_green,
            R.id.dateRecommendBtn to R.drawable.rounded_border_pink,
            R.id.soloEatBtn to R.drawable.rounded_border_tealed_700,
            R.id.kindOwnerBtn to R.drawable.rounded_border_purple_200,
            R.id.interiorBtn to R.drawable.rounded_border_orange
        )

        buttonBackgrounds.forEach { (id, backgroundRes) ->
            val button: Button = findViewById(id)
            button.background = ContextCompat.getDrawable(this, backgroundRes)
        }
    }


    // 리뷰 등록 API 호출
    private fun submitReview() {
        val reviewBody = reviewInput.text.toString()
        val ratingValue = currentRating
        val dateTime = LocalDateTime.now().toString()
        val accessToken = tokenManager.getAccessToken()

        // 입력 검증
        if (reviewBody.isBlank()) {
            Toast.makeText(this, "리뷰 내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        if (ratingValue == 0f) {
            Toast.makeText(this, "별점을 선택해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        if (accessToken.isNullOrEmpty()) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val reviewRequest = ReviewRequest(
            restaurantId = 1,
            body = reviewBody,
            rating = ratingValue.toDouble(),
            dateTime = dateTime,
            isReviewEventCheck = reviewEventCheck,
            tag = selectedTag // 선택 사항
        )

        // 로그 메시지 출력 (전송 데이터 확인)
        Log.d("SubmitReview", "ReviewRequest: $reviewRequest")
        Log.d("SubmitReview", "AccessToken: Bearer $accessToken")
        Log.d("SubmitReview", "Tag : $selectedTag")

        // API 호출
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = RetrofitInstance.restaurantApi.createReview(
                    reviewRequest,
                    "Bearer $accessToken" // Bearer 포함
                )

                // 응답 처리
                if (response.isSuccessful) {
                    Log.d("SubmitReview", "Response Success: ${response.body()}")
                    Toast.makeText(this@ReviewWriteActivity, "리뷰가 등록되었습니다!", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("SubmitReview", "Response Failed: Code=${response.code()}, Message=${response.errorBody()?.string()}")
                    Toast.makeText(this@ReviewWriteActivity, "등록 실패: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("SubmitReview", "Server Error: ${e.message}")
                Toast.makeText(this@ReviewWriteActivity, "서버 오류 발생: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
