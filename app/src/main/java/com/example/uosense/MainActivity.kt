package com.example.uosense

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.uosense.databinding.ActivityMainBinding
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.NaverMapSdk
import com.naver.maps.map.overlay.Marker

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 뷰 바인딩 초기화
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 네이버 지도 초기화
        NaverMapSdk.getInstance(this).client = NaverMapSdk.NaverCloudPlatformClient("s78aa7asq0")

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance().also {
                supportFragmentManager.beginTransaction().add(R.id.map, it).commit()
            }

        mapFragment.getMapAsync { naverMap ->
            addMarkers(naverMap)
        }

        // 검색창 이벤트
        binding.searchBar.setOnEditorActionListener { _, _, _ ->
            val query = binding.searchBar.text.toString()
            // 검색 로직 추가해야 함
            true
        }

        // 'door_type' 설정 버튼 클릭 이벤트
        binding.doorTypeButton.setOnClickListener {
            // Door Type 설정 로직 추가
        }

        // MyPage 이동 버튼 클릭 이벤트
        binding.myPageButton.setOnClickListener {
            startActivity(Intent(this, MyPageActivity::class.java))
        }
    }

    private fun addMarkers(naverMap: NaverMap) {
        // 예시로 마커 데이터 생성
        val restaurantMarkers = listOf(
            Pair(1, LatLng(37.5666102, 126.9783881)), // 식당 ID: 1
            Pair(2, LatLng(37.5656102, 126.9763881))  // 식당 ID: 2
        )

        restaurantMarkers.forEach { (restaurantId, location) ->
            val marker = Marker().apply {
                position = location
                map = naverMap
                tag = restaurantId // 마커에 식당 ID를 태그로 저장
            }

            marker.setOnClickListener {
                val intent = Intent(this, RestaurantDetailActivity::class.java)
                intent.putExtra("restaurantId", marker.tag as Int) // ID 전달
                startActivity(intent)
                true
            }
        }
    }

}
