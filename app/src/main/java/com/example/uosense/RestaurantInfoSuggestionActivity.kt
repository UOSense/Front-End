package com.example.uosense

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.uosense.adapters.InfoSuggestionPagerAdapter
import com.example.uosense.databinding.ActivityRestaurantInfoSuggestionBinding
import com.example.uosense.fragments.BasicInfoFragment
import com.example.uosense.fragments.BusinessHoursFragment
import com.example.uosense.fragments.ProductMenuFragment
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
                    val currentFragment = getCurrentFragment()

                    if (currentFragment is ProductMenuFragment) {
                        Log.d("ActiveFragment", "현재 프래그먼트: 상품/메뉴")
                        currentFragment.sendProductMenuToServer()
                    } else if (currentFragment is BusinessHoursFragment) {
                        Log.d("ActiveFragment", "현재 프래그먼트: 영업시간")
                        currentFragment.sendBusinessHoursToServer()
                    } else if (currentFragment is BasicInfoFragment) {
                        Log.d("ActiveFragment", "현재 프래그먼트: 기본정보")
                        currentFragment.sendBasicInfoToServer()
                    } else {
                        Log.e("ActiveFragment", "알 수 없는 프래그먼트")
                        Toast.makeText(this, "알 수 없는 프래그먼트입니다.", Toast.LENGTH_SHORT).show()
                    }
                }
    }
    // 현재 활성화된 프래그먼트를 가져오는 메서드
    private fun getCurrentFragment(): Fragment? {
        val currentPosition = binding.viewPager.currentItem
        return supportFragmentManager.findFragmentByTag("f$currentPosition")
    }
}
