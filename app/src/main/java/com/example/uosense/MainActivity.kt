package com.example.uosense

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.uosense.databinding.ActivityMainBinding

import com.example.uosense.models.RestaurantListResponse
import com.example.uosense.models.RestaurantRequest
import com.example.uosense.network.RetrofitInstance
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
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

    private val bounds = LatLngBounds.Builder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        RetrofitInstance.setBaseUrl("http://10.0.2.2:8080")


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
            this.naverMap = naverMap
            initializeMap()
            loadMockRestaurants() // 데이터 로드 및 마커 생성
        }




        binding.svSearch.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
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

    private fun initializeMap() {
        //초기 지도 중심 설정
        val initialPosition = LatLng(37.5834643, 127.0536246)
        moveCameraToLocation(initialPosition.latitude, initialPosition.longitude)
    }

    private fun getLatLngFromAddress(address: String, callback: (Double?, Double?) -> Unit) {
        //도로명 주소 -> 위도, 경도 변환
        val client = OkHttpClient()
        val url = "https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode?query=${address}"
        println(url)
        val request = Request.Builder()
            .url(url)
            .addHeader("X-NCP-APIGW-API-KEY-ID", "s78aa7asq0")
            .addHeader("X-NCP-APIGW-API-KEY", "WGmu5zQXqTGWOy7Bj9PWwrD8HeQezlBvZ675Q24K")
            .build()

        Thread {
            try {
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                val json = JSONObject(responseBody!!)
                val addresses = json.getJSONArray("addresses")
                if (addresses.length() > 0) {
                    val location = addresses.getJSONObject(0)
                    val latitude = location.getDouble("y")
                    val longitude = location.getDouble("x")
                    callback(latitude, longitude)
                } else {
                    callback(null, null)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                callback(null, null)
            }
        }.start()
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
                        Toast.makeText(this@MainActivity, "식당 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
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
            /*
            ** 서버 연동시
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
                        updateRestaurantCoordinates(restaurant.id, updatedRequest)

                        // 마커 추가
                        val marker = Marker().apply {
                            position = LatLng(lat, lng)
                            map = naverMap
                            captionText = restaurant.name
                            tag = restaurant.id
                        }

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
        }

        moveCameraToFitAllMarkers()

         */
            val marker = Marker().apply {
                position = LatLng(restaurant.latitude, restaurant.longitude)
                map = naverMap
                captionText = restaurant.name
                tag = restaurant.id // 태그로 식별자 설정
            }

            // 마커 클릭 이벤트 설정
            marker.setOnClickListener {
                // RestaurantDetailActivity로 이동
                val intent = Intent(this@MainActivity, RestaurantDetailActivity::class.java).apply {
                    putExtra("restaurantId", restaurant.id)
                    // 서버 연동 시에 추가 데이터도 intent에 포함 가능
                    // putExtra("restaurantData", Gson().toJson(restaurant))
                }
                startActivity(intent)
                true // 이벤트 처리 완료를 알림
            }

            restaurantMarkers.add(marker)
        }

        moveCameraToFitAllMarkers()
    }




    private fun moveCameraToLocation(latitude: Double, longitude: Double){
        val cameraUpdate = CameraUpdate.scrollTo(LatLng(latitude, longitude))
        naverMap.moveCamera(cameraUpdate)
    }

    private fun searchRestaurants(keyword: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.restaurantApi.searchRestaurants(keyword, "DEFAULT")
                withContext(Dispatchers.Main) {
                    if (response.isNotEmpty()) {
                        val intent = Intent(this@MainActivity, RestaurantListActivity::class.java).apply {
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


    private fun updateRestaurantCoordinates(restaurantId: Int, updatedRequest: RestaurantRequest) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.restaurantApi.editRestaurant(restaurantId, updatedRequest)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@MainActivity, "좌표가 업데이트되었습니다.", Toast.LENGTH_SHORT).show()
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
            val bounds = LatLngBounds.Builder()
            restaurantMarkers.forEach { marker ->
                bounds.include(marker.position)
            }
            val cameraUpdate = CameraUpdate.fitBounds(bounds.build())
            naverMap.moveCamera(cameraUpdate)
        }
    }

    private fun loadMockRestaurants() {
        // Mock 데이터 준비
        val mockRestaurants = listOf(
            RestaurantListResponse(
                id = 1,
                name = "Mock Restaurant 1",
                longitude = 127.0536246,
                latitude = 37.5834643,
                address = "서울시 중랑구 Mock 주소 1",
                rating = 4.5,
                category = "한식",
                reviewCount = 10,
                bookmarkCount = 5,
                restaurantImage = null,
                doorType = "정문",
                phoneNumber = "010-1234-5678"
            ),
            RestaurantListResponse(
                id = 2,
                name = "Mock Restaurant 2",
                longitude = 127.0564010,
                latitude = 37.5869791,
                address = "서울시 중랑구 Mock 주소 2",
                rating = 3.8,
                category = "중식",
                reviewCount = 20,
                bookmarkCount = 10,
                restaurantImage = null,
                doorType = "쪽문",
                phoneNumber = "010-8765-4321"
            ),
            RestaurantListResponse(
                id = 3,
                name = "Mock Restaurant 3",
                longitude = 127.0606581,
                latitude = 37.5869320,
                address = "서울시 중랑구 Mock 주소 3",
                rating = 4.0,
                category = "양식",
                reviewCount = 15,
                bookmarkCount = 8,
                restaurantImage = null,
                doorType = "후문",
                phoneNumber = "010-1122-3344"
            )
        )

        // 마커 추가 및 카메라 이동 호출
        addMarkersToMap(mockRestaurants)
    }



}
