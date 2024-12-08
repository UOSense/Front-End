package com.example.uosense.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.gridlayout.widget.GridLayout
import com.example.uosense.R
import com.example.uosense.databinding.FragmentBasicInfoBinding

class BasicInfoFragment : Fragment() {

    private var _binding: FragmentBasicInfoBinding? = null
    private val binding get() = _binding!!

    // 이미지 선택 런처
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { imageUri: Uri? ->
        if (imageUri != null) {
            addImageToGridLayout(imageUri)
            Toast.makeText(requireContext(), "이미지가 업로드되었습니다!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBasicInfoBinding.inflate(inflater, container, false)

        // 업로드 버튼 클릭 리스너
        binding.uploadImageBtn.setOnClickListener {
            checkStoragePermissionAndPickImage()
        }

        return binding.root
    }

    // 권한 확인 및 이미지 선택
    private fun checkStoragePermissionAndPickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                pickImageLauncher.launch("image/*")
            } else {
                requestPermissions(
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                    READ_MEDIA_IMAGES_REQUEST_CODE
                )
            }
        } else {
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
    private fun addImageToGridLayout(imageUri: Uri) {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val READ_MEDIA_IMAGES_REQUEST_CODE = 101
    }
}
