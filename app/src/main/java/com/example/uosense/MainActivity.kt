package com.example.uosense

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.uosense.databinding.ActivityMainBinding
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.NaverMapSdk
import com.naver.maps.map.overlay.Marker

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var naverMap: NaverMap
    private val restaurantMarkers = mutableListOf<Marker>()

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
            initializeMap()
        }

        // 검색창 이벤트
//        binding.searchBar.setOnEditorActionListener { _, _, _ ->
//            val query = binding.searchBar.text.toString()
//            if(query.isNotBlank()) {
//                searchRestaurants(query)
//            }else{
//                Toast.makeText(this, "검색어를 입력해주세요.", Toast.LENGTH_SHORT).show()
//            }
//            true
//        }

        setupFilterButtons()

        // MyPage 이동 버튼 클릭 이벤트
//        binding.myPageBtn.setOnClickListener {
//            startActivity(Intent(this, MyPageActivity::class.java))
//        }
    }

    private fun initializeMap() {
        //초기 지도 중심 설정
        val initialPosition = LatLng(37.5834643, 127.0536246)
        moveCameraToLocation(initialPosition.latitude, initialPosition.longitude)
    }

    //버튼을 누르면 위치 이동
    private fun setupFilterButtons() {
        // 정문 버튼 클릭
        binding.doorTypeButton1.setOnClickListener {
            moveCameraToLocation(37.5834643, 127.0536246) // 정문 좌표
            Toast.makeText(this, "정문 위치로 이동합니다.", Toast.LENGTH_SHORT).show()
        }

        // 쪽문 버튼 클릭
        binding.doorTypeButton2.setOnClickListener {
            moveCameraToLocation(37.5869791, 127.0564010) // 쪽문 좌표
            Toast.makeText(this, "쪽문 위치로 이동합니다.", Toast.LENGTH_SHORT).show()
        }

        // 후문 버튼 클릭
        binding.doorTypeButton3.setOnClickListener {
            moveCameraToLocation(37.5869320, 127.0606581) // 후문 좌표
            Toast.makeText(this, "후문 위치로 이동합니다.", Toast.LENGTH_SHORT).show()
        }

        // 남문 버튼 클릭
        binding.doorTypeButton4.setOnClickListener {
            moveCameraToLocation(37.5775540, 127.0578147) // 남문 좌표
            Toast.makeText(this, "남문 위치로 이동합니다.", Toast.LENGTH_SHORT).show()
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

//            restaurantMarkers.add(marker)
        }
    }

    private fun moveCameraToLocation(latitude: Double, longitude: Double){
        val cameraUpdate = CameraUpdate.scrollTo(LatLng(latitude, longitude))
        naverMap.moveCamera(cameraUpdate)
    }

    private fun searchRestaurants(query: String) {
        // 검색 로직 예시
        val filteredMarkers = restaurantMarkers.filter { marker ->
            marker.captionText.contains(query, ignoreCase = true)
        }

        if (filteredMarkers.isNotEmpty()) {
            val firstMarker = filteredMarkers.first()
            moveCameraToLocation(firstMarker.position.latitude, firstMarker.position.longitude)
            Toast.makeText(this, "${filteredMarkers.size}개의 검색 결과가 있습니다.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

}
