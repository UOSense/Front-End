package com.example.uosense

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.uosense.databinding.ActivityMainBinding
import com.example.uosense.models.RestaurantInfo

import com.example.uosense.models.RestaurantListResponse
import com.example.uosense.models.RestaurantRequest
import com.example.uosense.network.RetrofitInstance
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.NaverMapSdk
import com.naver.maps.map.overlay.Marker

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var naverMap: NaverMap
    private val restaurantMarkers = mutableListOf<Marker>()
    private lateinit var userProfileBtn: Button

    private val bounds = LatLngBounds.Builder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // 뷰 바인딩 초기화
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userProfileBtn = findViewById(R.id.userProfileBtn)
        // 버튼 클릭 이벤트
        userProfileBtn.setOnClickListener {
            navigateToMyPage()
        }




        // 네이버 지도 초기화
        NaverMapSdk.getInstance(this).client = NaverMapSdk.NaverCloudPlatformClient("s78aa7asq0")

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance().also {
                supportFragmentManager.beginTransaction().add(R.id.map, it).commit()
            }

        mapFragment.getMapAsync { naverMap ->
            this.naverMap = naverMap
            Log.d("NAVER_MAP_INIT", "NaverMap 초기화 성공")
            initializeMap()
            loadRestaurants()
        }





        binding.svSearch.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val trimmedQuery = query?.trim()
                if (!trimmedQuery.isNullOrBlank()) {
                    searchRestaurants(trimmedQuery)
                } else {
                    Toast.makeText(this@MainActivity, "검색어를 입력해주세요.", Toast.LENGTH_SHORT).show()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                /*
                ** 실시간 검색 로직 구현 굳이?
                 */
                return false
            }
        })


        setupFilterButtons()


        // MyPage 이동 버튼 클릭 이벤트
//        binding.myPageBtn.setOnClickListener {
//            startActivity(Intent(this, MyPageActivity::class.java))
//        }
    }


    private fun navigateToMyPage() {
        val intent = Intent(this, MyPageActivity::class.java)
        startActivity(intent) // MyPageActivity로 이동
    }

    private fun initializeMap() {
        val initialPosition = LatLng(37.5834643, 127.0536246) // 서울시립대 정문 위치
        val cameraPosition = CameraPosition(initialPosition, 16.0) // 줌 레벨 15.0 설정
        naverMap.cameraPosition = cameraPosition
    }


    private fun getLatLngFromAddress(address: String, callback: (Double?, Double?) -> Unit) {
        val client = OkHttpClient()
        val url = "https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode?query=${address}"
        println(url)

        val request = Request.Builder()
            .url(url)
            .addHeader("X-NCP-APIGW-API-KEY-ID", "s78aa7asq0")
            .addHeader("X-NCP-APIGW-API-KEY", "WGmu5zQXqTGWOy7Bj9PWwrD8HeQezlBvZ675Q24K")
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                val json = JSONObject(responseBody ?: "")
                val addresses = json.getJSONArray("addresses")

                if (addresses.length() > 0) {
                    val location = addresses.getJSONObject(0)
                    val latitude = location.getDouble("y")
                    val longitude = location.getDouble("x")

                    withContext(Dispatchers.Main) {
                        callback(latitude, longitude)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        callback(null, null)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    callback(null, null)
                }
            }
        }
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

    private fun loadRestaurants() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val restaurants = RetrofitInstance.restaurantApi.getAllRestaurants(null, null)
                withContext(Dispatchers.Main) {
                    if (restaurants.isNotEmpty()) {
                        addMarkersToMap(restaurants)
                    } else {
                        Toast.makeText(this@MainActivity, "식당 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "데이터 로드 중 오류 발생", Toast.LENGTH_SHORT).show()
                }
                e.printStackTrace()
            }
        }
    }

    private fun addMarkersToMap(restaurantList: List<RestaurantListResponse>) {
        restaurantList.forEach { restaurant ->
            if (restaurant.latitude != 0.0 && restaurant.longitude != 0.0) {
                // 마커 추가
                val marker = Marker().apply {
                    position = LatLng(restaurant.latitude, restaurant.longitude)
                    map = naverMap
                    captionText = restaurant.name
                    tag = restaurant.id
                }

                // 마커 클릭 이벤트 설정
                marker.setOnClickListener {
                    fetchRestaurantDetails(restaurant.id)
                    true
                }
                restaurantMarkers.add(marker)
            } else {
                // Geocoding API 호출
                getLatLngFromAddress(restaurant.address) { lat, lng ->
                    if (lat != null && lng != null) {
                        val marker = Marker().apply {
                            position = LatLng(lat, lng)
                            map = naverMap
                            captionText = restaurant.name
                            tag = restaurant.id
                        }

                        // 마커 클릭 이벤트 설정
                        marker.setOnClickListener {
                            fetchRestaurantDetails(restaurant.id)
                            true
                        }
                        restaurantMarkers.add(marker)
                    }
                }
            }
        }
        moveCameraToFitAllMarkers()
    }
    private fun fetchRestaurantDetails(restaurantId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.restaurantApi.getRestaurantById(restaurantId)
                withContext(Dispatchers.Main) {
                    navigateToRestaurantDetail(response)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "식당 정보를 불러오지 못했습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                e.printStackTrace()
            }
        }
    }

    private fun navigateToRestaurantDetail(restaurantInfo: RestaurantInfo) {
        val intent = Intent(this, RestaurantDetailActivity::class.java).apply {
            putExtra("restaurantInfo", restaurantInfo)
        }
        startActivity(intent)
    }

    /*
    private fun addMarkersToMap(restaurantList: List<RestaurantListResponse>) {
        restaurantList.forEach { restaurant ->
            // 마커 추가
            val marker = Marker()
            marker.position = LatLng(restaurant.latitude, restaurant.longitude)
            marker.captionText = restaurant.name

            if (restaurant.latitude == 0.0 && restaurant.longitude == 0.0) {
                getLatLngFromAddress(restaurant.address) { lat, lng ->
                    if (lat != null && lng != null) {
                        // RestaurantRequest 객체 생성
                        val updatedRequest = RestaurantRequest(
                            id = restaurant.id,
                            name = restaurant.name,
                            doorType = null, // DoorType이 없으면 기본값으로 설정
                            latitude = lat,
                            longitude = lng,
                            address = restaurant.address,
                            phoneNumber = null, // PhoneNumber가 없으면 기본값으로 설정
                            category = restaurant.category,
                            subDescription = null, // SubDescription 기본값
                            description = "" // 기본값 설정
                        )
                        // API 호출
                        // updateRestaurantCoordinates(restaurant.id, updatedRequest)

                        /*
                         마커 추가 (업데이트 후 등록 시)
                         */

                        /*val marker = Marker().apply {
                            position = LatLng(lat, lng)
                            map = naverMap
                            captionText = restaurant.name
                            tag = restaurant.id
                        }
                        */


                        // 마커 클릭 이벤트 설정
                        marker.setOnClickListener {
                            // RestaurantDetailActivity로 이동
                            val intent = Intent(this@MainActivity, RestaurantDetailActivity::class.java).apply {
                                putExtra("restaurantId", restaurant.id)
                            }
                            startActivity(intent)
                            true // 이벤트 처리 완료를 알림
                        }
                        restaurantMarkers.add(marker)
                    }
                }
            } else {
                // 경도와 위도가 이미 존재하는 경우 바로 마커 추가
                val marker = Marker().apply {
                    position = LatLng(restaurant.latitude, restaurant.longitude)
                    map = naverMap
                    captionText = restaurant.name
                    tag = restaurant.id
                }
                restaurantMarkers.add(marker)
            }
            runOnUiThread {
                marker.setMap(naverMap) // 메인 스레드에서 호출
            }
        }

        moveCameraToFitAllMarkers()
    }
    */


    private fun moveCameraToLocation(latitude: Double, longitude: Double) {
        val cameraUpdate = CameraUpdate.scrollTo(LatLng(latitude, longitude))
        naverMap.moveCamera(cameraUpdate)
    }

    private fun searchRestaurants(keyword: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.restaurantApi.searchRestaurants(keyword, "DEFAULT")
                withContext(Dispatchers.Main) {
                    if (response.isNotEmpty()) {
                        val intent =
                            Intent(this@MainActivity, RestaurantListActivity::class.java).apply {
                                putParcelableArrayListExtra("restaurantList", ArrayList(response))
                            }
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@MainActivity, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "검색 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
                e.printStackTrace()
            }
        }
    }

    /*
    **하지마 시발
     */
    private fun updateRestaurantCoordinates(restaurantId: Int, updatedRequest: RestaurantRequest) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // updatedRequest를 전달하도록 수정
                val response = RetrofitInstance.restaurantApi.editRestaurant(updatedRequest)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@MainActivity, "좌표가 업데이트되었습니다.", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Log.e("API_ERROR", "업데이트 실패: ${response.errorBody()?.string()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "업데이트 중 오류 발생", Toast.LENGTH_SHORT).show()
                }
                e.printStackTrace()
            }
        }
    }


    private fun moveCameraToFitAllMarkers() {
        if (restaurantMarkers.isNotEmpty()) {
            val bounds = LatLngBounds.Builder().apply {
                restaurantMarkers.forEach { marker ->
                    include(marker.position)
                }
            }.build()

            val cameraUpdate = CameraUpdate.fitBounds(bounds, 100) // 패딩 추가
            naverMap.moveCamera(cameraUpdate)
        }
    }
}






