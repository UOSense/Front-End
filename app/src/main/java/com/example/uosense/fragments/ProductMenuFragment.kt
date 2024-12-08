package com.example.uosense.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.gridlayout.widget.GridLayout
import com.example.uosense.R
import com.example.uosense.databinding.FragmentProductMenuBinding

class ProductMenuFragment : Fragment() {

    private var _binding: FragmentProductMenuBinding? = null
    private val binding get() = _binding!!

    // Activity Result Launcher for Image Picker
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { imageUri: Uri? ->
        if (imageUri != null) {
            addImageToContainer(imageUri)
            Toast.makeText(requireContext(), "이미지가 업로드되었습니다!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductMenuBinding.inflate(inflater, container, false)

        // 이미지 권한 요청
        checkStoragePermission()

        // 이미지 업로드 버튼 클릭 리스너
        binding.uploadImageBtn.setOnClickListener {
            checkStoragePermissionAndPickImage()
        }

        // 가격 정보 추가 버튼 클릭 리스너
        binding.addCircleBtn.setOnClickListener {
            addPriceRow()
        }

        return binding.root
    }

    // 권한 요청 메서드
    private fun checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                    READ_MEDIA_IMAGES_REQUEST_CODE
                )
            }
        }
    }

    // 권한 확인 및 이미지 선택
    private fun checkStoragePermissionAndPickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13 이상 권한 확인
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                pickImageLauncher.launch("image/*")
            } else {
                // 권한 요청
                requestPermissions(
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                    READ_MEDIA_IMAGES_REQUEST_CODE
                )
            }
        } else {
            // Android 13 미만에서는 권한 요청 필요 없음
            pickImageLauncher.launch("image/*")
        }
    }

    // 권한 요청 결과 처리
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_MEDIA_IMAGES_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImageLauncher.launch("image/*")
            } else {
                Toast.makeText(
                    requireContext(),
                    "이미지 접근 권한이 필요합니다.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // 동적으로 이미지 추가
    private fun addImageToContainer(imageUri: Uri) {
        // 최대 이미지 개수 제한
        val maxImages = 4
        val currentImageCount = binding.imageContainer.childCount

        if (currentImageCount > maxImages) {
            Toast.makeText(requireContext(), "최대 $maxImages 장의 이미지만 추가할 수 있습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // 이미지 뷰 생성 및 설정
        val imageView = ImageView(requireContext()).apply {
            setImageURI(imageUri)
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.CENTER_CROP

            // 사진 개수에 따른 크기 조절
            layoutParams = calculateGridLayoutParams()
        }

        // 이미지 추가
        binding.imageContainer.addView(imageView)

        // 이미지 힌트 가리기
        binding.imageHint.visibility = View.GONE
    }

    // 사진 개수에 따른 이미지 크기 계산 메서드
    private fun calculateGridLayoutParams(): GridLayout.LayoutParams {
        val gridLayout = binding.imageContainer
        val totalWidth = gridLayout.width
        val itemWidth = totalWidth / 2
        val itemHeight = gridLayout.height / 2

        return GridLayout.LayoutParams().apply {
            width = itemWidth
            height = itemHeight
            setMargins(8, 8, 8, 8)
        }
    }

    // 가격 정보 행 추가 메서드
    private fun addPriceRow() {
        val context = requireContext()

        // 동적 레이아웃 생성
        val priceRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        // 메뉴/상품명 입력 필드
        val menuInput = EditText(context).apply {
            hint = "메뉴, 상품명"
            setPadding(16, 8, 16, 8)
            background = ContextCompat.getDrawable(context, R.drawable.rounded_input_background)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.7f)
            setTextColor(ContextCompat.getColor(context, R.color.black))
            setHintTextColor(ContextCompat.getColor(context, R.color.gray))
        }

        // 금액 입력 필드
        val priceInput = EditText(context).apply {
            hint = "금액"
            inputType = InputType.TYPE_CLASS_NUMBER
            setPadding(16, 8, 16, 8)
            background = ContextCompat.getDrawable(context, R.drawable.rounded_input_background)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            setTextColor(ContextCompat.getColor(context, R.color.black))
            setHintTextColor(ContextCompat.getColor(context, R.color.gray))
        }

        // 삭제 버튼
        val deleteButton = Button(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                dpToPx(30), // 가로 30dp
                dpToPx(30)  // 세로 30dp
            ).apply {
                setMargins(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8))
            }
            background = ContextCompat.getDrawable(context, R.drawable.ic_delete)
            contentDescription = "삭제 버튼"
            setOnClickListener {
                binding.priceContainer.removeView(priceRow)
                Toast.makeText(context, "항목이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // 동적으로 생성된 뷰 추가
        priceRow.addView(menuInput)
        priceRow.addView(priceInput)
        priceRow.addView(deleteButton)

        // 컨테이너에 추가
        binding.priceContainer.addView(priceRow)
    }

    // dp 값을 px 값으로 변환하는 함수
    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics
        ).toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val READ_MEDIA_IMAGES_REQUEST_CODE = 101
    }
}
