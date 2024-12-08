package com.example.uosense.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.uosense.R
import com.example.uosense.databinding.FragmentBusinessHoursBinding

class BusinessHoursFragment : Fragment() {

    private var _binding: FragmentBusinessHoursBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBusinessHoursBinding.inflate(inflater, container, false)

        // 수정할 정보 입력 버튼 클릭 리스너
        binding.addInfoBtn.setOnClickListener {
            addNewInfoField()
        }

        return binding.root
    }

    // 동적 EditText + 삭제 버튼 추가 메서드
    private fun addNewInfoField() {
        val context = requireContext()

        // 새로 추가될 레이아웃
        val infoRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }
        }

        // EditText 생성
        val newEditText = EditText(context).apply {
            hint = "ex) 화요일 3~5시 브레이크타임"
            setPadding(16, 8, 16, 8)
            background = ContextCompat.getDrawable(context, R.drawable.rounded_input_background)
            layoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            )
            setTextColor(ContextCompat.getColor(context, R.color.black))
            setHintTextColor(ContextCompat.getColor(context, R.color.gray))
        }

        // 삭제 버튼 생성
        val deleteButton = Button(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                dpToPx(30), dpToPx(30)
            ).apply {
                setMargins(dpToPx(8), 0, 0, 0)
            }
            background = ContextCompat.getDrawable(context, R.drawable.ic_delete)
            contentDescription = "삭제 버튼"
            setOnClickListener {
                binding.customInfoContainer.removeView(infoRow)
                Toast.makeText(context, "정보 입력란이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // 레이아웃에 뷰 추가
        infoRow.addView(newEditText)
        infoRow.addView(deleteButton)

        // 컨테이너에 추가
        binding.customInfoContainer.addView(infoRow)
    }

    // dp 변환 메서드
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
