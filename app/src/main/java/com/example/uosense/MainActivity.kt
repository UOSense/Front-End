package com.example.uosense

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentContainerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.CameraUpdate

import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback

import com.naver.maps.map.overlay.Marker


import com.example.uosense.adapters.SearchResultAdapter
import com.example.uosense.models.Restaurant

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchResultAdapter: SearchResultAdapter
    private lateinit var searchButton: Button
    private lateinit var searchField: EditText

    private val markers = mutableListOf<Marker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NaverMapSdk.getInstance(this).client =
            NaverMapSdk.NaverCloudPlatformClient("s78aa7asq0")
        setContentView(R.layout.activity_main)

        // MapFragment 가져오기 또는 추가
        var mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as? MapFragment
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance()
            supportFragmentManager.beginTransaction()
                .add(R.id.map_fragment, mapFragment)
                .commit()
        }


        searchField = findViewById(R.id.searchField)
        searchButton = findViewById(R.id.searchButton)
        recyclerView = findViewById(R.id.searchResults)

        // RecyclerView 초기화
        recyclerView.layoutManager = LinearLayoutManager(this)
        searchResultAdapter = SearchResultAdapter { restaurant ->
            moveToMarker(restaurant)
        }
        recyclerView.adapter = searchResultAdapter

        // 검색 버튼 클릭 이벤트
        searchButton.setOnClickListener {
            val query = searchField.text.toString()
            if (query.isNotEmpty()) {
                searchRestaurants(query)
            }
        }
    }

    override fun onMapReady(map: NaverMap) {
        naverMap = map
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
        naverMap.locationSource = locationSource
        naverMap.uiSettings.isLocationButtonEnabled = true

        // 초기 지도 위치 설정
        val seoul = LatLng(37.5665, 126.9780)
        naverMap.moveCamera(CameraUpdate.scrollTo(seoul))
    }

    private fun searchRestaurants(query: String) {
        // 기존 마커 제거
        markers.forEach { it.map = null }
        markers.clear()

        val mockData = listOf(
            Restaurant(1, "Sushi House", "123 Sushi St.", 37.567, 126.978, 4.5f, "Delicious"),
            Restaurant(2, "Pasta Palace", "456 Pasta Rd.", 37.565, 126.976, 4.2f, "Not Bad")
        )

        val filteredResults = mockData.filter { it.name.contains(query, ignoreCase = true) }

        filteredResults.forEach { restaurant ->
            val marker = Marker()
            marker.position = LatLng(restaurant.latitude, restaurant.longitude)
            marker.map = naverMap
            marker.captionText = restaurant.name
            markers.add(marker)
        }

        searchResultAdapter.submitList(filteredResults)
        recyclerView.visibility = if (filteredResults.isNotEmpty()) View.VISIBLE else View.GONE
    }

    private fun moveToMarker(restaurant: Restaurant) {
        val position = LatLng(restaurant.latitude, restaurant.longitude)
        naverMap.moveCamera(CameraUpdate.scrollTo(position))
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    /*
    private fun fetchRestaurants(query: String) {
    // 네트워크 요청 또는 데이터베이스 검색 로직 추가
    }
     */

    /*
    검색 결과가 없는 경우 -> Recycler는 숨겨야함
    if (filteredResults.isEmpty()) {
    Toast.makeText(this, "No results found", Toast.LENGTH_SHORT).show()
    }
     */

    /*
    사용자가 마커 누르면 레스토랑 상세 정보 볼수있어야함
    marker.setOnClickListener {
    Toast.makeText(this, "Selected: ${restaurant.name}", Toast.LENGTH_SHORT).show()
    true
    }
     */

    /*
    위치 서비스 권한 처리 제대로 안된다? 앱 종료
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
    if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
        if (!locationSource.isActivated) {
            naverMap.locationTrackingMode = LocationTrackingMode.None
        }
        return
    }
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
     */
}
