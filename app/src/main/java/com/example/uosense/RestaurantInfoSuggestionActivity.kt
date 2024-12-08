package com.example.uosense

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.uosense.adapters.InfoSuggestionPagerAdapter
import com.example.uosense.databinding.ActivityRestaurantInfoSuggestionBinding
import com.google.android.material.tabs.TabLayoutMediator

class RestaurantInfoSuggestionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRestaurantInfoSuggestionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRestaurantInfoSuggestionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ViewPager와 TabLayout 연결
        val adapter = InfoSuggestionPagerAdapter(this)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "상품/메뉴"
                1 -> "영업시간"
                2 -> "전화번호"
                else -> "기본정보"
            }
        }.attach()

        // 뒤로 가기 버튼
        binding.backBtn.setOnClickListener {
            finish()
        }

        // 저장 버튼
        binding.saveBtn.setOnClickListener {
            Toast.makeText(this, "저장되었습니다!", Toast.LENGTH_SHORT).show()
        }
    }
}
