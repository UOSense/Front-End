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
import com.example.uosense.databinding.FragmentPhoneNumberBinding

class PhoneNumberFragment : Fragment() {

    private var _binding: FragmentPhoneNumberBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhoneNumberBinding.inflate(inflater, container, false)

        // 추가 버튼 클릭 리스너
        binding.addCircleBtn.setOnClickListener {
            addPhoneNumberField()
        }

        return binding.root
    }

    // 전화번호 필드 동적 추가 메서드
    private fun addPhoneNumberField() {
        val context = requireContext()

        // 새로 추가될 전화번호 입력 레이아웃
        val phoneRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 12)
            }
        }

        // 동적 EditText
        val phoneNumberInput = EditText(context).apply {
            hint = "전화번호 입력"
            inputType = android.text.InputType.TYPE_CLASS_PHONE
            background = ContextCompat.getDrawable(context, R.drawable.rounded_input_background)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            setPadding(16, 8, 16, 8)
            setTextColor(ContextCompat.getColor(context, R.color.black))
            setHintTextColor(ContextCompat.getColor(context, R.color.gray))
        }

        // 삭제 버튼
        val deleteButton = Button(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                dpToPx(30), dpToPx(30)
            ).apply {
                setMargins(dpToPx(8), 0, 0, 0)
            }
            background = ContextCompat.getDrawable(context, R.drawable.ic_delete)
            contentDescription = "삭제 버튼"
            setOnClickListener {
                binding.phoneNumberContainer.removeView(phoneRow)
                Toast.makeText(context, "전화번호 필드가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // View 추가
        phoneRow.addView(phoneNumberInput)
        phoneRow.addView(deleteButton)
        binding.phoneNumberContainer.addView(phoneRow)
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
