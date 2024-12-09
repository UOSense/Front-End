package com.example.uosense


import TokenManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.uosense.databinding.ActivityMainBinding
import com.example.uosense.models.RestaurantInfo
import com.naver.maps.map.util.FusedLocationSource

import com.example.uosense.models.RestaurantListResponse
import com.example.uosense.models.RestaurantRequest
import com.example.uosense.network.RetrofitInstance
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
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

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location.distanceBetween
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import com.naver.maps.map.overlay.OverlayImage

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlin.math.log


class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private lateinit var binding: ActivityMainBinding
    private lateinit var naverMap: NaverMap
    private lateinit var locationPermissionLauncher: ActivityResultLauncher<Array<String>>
    private var userMarker: Marker? = null
    private val restaurantMarkers = mutableListOf<Marker>()
    private lateinit var locationSource: FusedLocationSource
    // 위치 추적 모드 관리 변수
    private var isLocationFixed = false

    private lateinit var tokenManager: TokenManager





    //검색을 위해서 새로운 변수
    private var selectedDoorType: String? = null
    private var isNearbySearchEnabled = false

    /**
     * 초기 상태 관리 및 버튼 토글 기능 추가
     */
    private var selectedButton: View? = null




    override fun onCreate(savedInstanceState: Bundle?) {
        // TokenManager 초기화
        tokenManager = TokenManager(this)

        // 리프레시 토큰 검증
        val refreshToken = tokenManager.getRefreshToken()
        if (refreshToken.isNullOrEmpty()) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            navigateToLoginActivity() // 로그인 화면으로 이동
            return
        }
        super.onCreate(savedInstanceState)

        // 뷰 바인딩 초기화
        binding = ActivityMainBinding.inflate(layoutInflater)

        binding.chkNearby.setOnCheckedChangeListener { _, isChecked ->
            isNearbySearchEnabled = isChecked
        }

        binding.doorTypeButton1.setOnClickListener {
            selectedDoorType = "정문"
            isLocationFixed = true
            moveCameraToLocation(37.5834643, 127.0536246)
            showToast("정문 위치로 이동합니다.")
        }

        binding.doorTypeButton2.setOnClickListener {
            selectedDoorType = "쪽문"
            isLocationFixed = true
            moveCameraToLocation(37.5869791, 127.0564010)
            showToast("쪽문 위치로 이동합니다.")
        }

        binding.doorTypeButton3.setOnClickListener {
            selectedDoorType = "후문"
            isLocationFixed = true
            moveCameraToLocation(37.5869320, 127.0606581)
            showToast("후문 위치로 이동합니다.")
        }

        binding.doorTypeButton4.setOnClickListener {
            selectedDoorType = "남문"
            isLocationFixed = true
            moveCameraToLocation(37.5775540, 127.0578147)
            showToast("남문 위치로 이동합니다.")
        }


        setContentView(binding.root)

        // 위치 서비스 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    Log.d("LOCATION_UPDATE", "위치 업데이트: ${location.latitude}, ${location.longitude}")
                }
            }
        }

        setupLocationPermissionLauncher()
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)


        // 네이버 지도 초기화
        NaverMapSdk.getInstance(this).client = NaverMapSdk.NaverCloudPlatformClient("s78aa7asq0")

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance().also {
                supportFragmentManager.beginTransaction().add(R.id.map, it).commit()
            }

        mapFragment.getMapAsync { naverMap ->
            this.naverMap = naverMap
            this.naverMap.locationSource = locationSource
            Log.d("NAVER_MAP_INIT", "NaverMap 초기화 성공")
            initializeMapWithoutPermission()

            requestLocationPermission()
        }

        binding.btnUserLocation.setOnClickListener {
            checkLocationPermissionAndMoveCamera()
        }

        setupSearch()

        setupFilterButtons()

        setupClickListeners()

        resetToInitialState()

        customizeSearchView()

        // 내 위치 버튼 클릭 시 위치 추적 재시작
        binding.btnUserLocation.setOnClickListener {
            enableUserLocation()
            isLocationFixed = false
            showToast("내 위치로 돌아갑니다.")
        }

        // "목록 보기" 버튼 클릭 시 동작 수정
        binding.categoryBtn.setOnClickListener {
            if (selectedDoorType.isNullOrBlank()) {
                loadAllRestaurants() // 전체 식당 목록 보기
            } else {
                loadRestaurantsByFilter(selectedDoorType!!)
            }
        }

        binding.userProfileBtn.setOnClickListener {
            val intent = Intent(this, MyPageActivity::class.java)
            startActivity(intent)
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

        // MyPage 이동 버튼 클릭 이벤트
//        binding.myPageBtn.setOnClickListener {
//            startActivity(Intent(this, MyPageActivity::class.java))
//        }
    }

    // 위치 권한 요청 초기화
    private fun setupLocationPermissionLauncher() {
        locationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            ) {
                Log.d("PERMISSION_RESULT", "위치 권한 허용됨")
                if (::naverMap.isInitialized) {
                    enableUserLocation()
                } else {
                    Log.e("PERMISSION_RESULT", "NaverMap 초기화가 안 됨")
                }
            } else {
                Log.d("PERMISSION_RESULT", "위치 권한 거부됨")
                if (::naverMap.isInitialized) {
                    initializeMapWithoutPermission()
                }
            }
        }
    }



    // 전체 식당 로딩 함수
    private fun loadAllRestaurants() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val restaurantList = RetrofitInstance.restaurantApi.getRestaurantList(
                    doorType = null,  // 전체 목록 보기
                    filter = "DEFAULT"
                )
                withContext(Dispatchers.Main) {
                    if (restaurantList.isNotEmpty()) {
                        navigateToRestaurantList(restaurantList)
                    } else {
                        showToast("식당 목록이 없습니다.")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("데이터 로드 중 오류가 발생했습니다.")
                }
                e.printStackTrace()
            }
        }
    }
    // 특정 DoorType 식당 로딩 함수
    private fun loadRestaurantsByFilter(doorType: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val restaurantList = RetrofitInstance.restaurantApi.getRestaurantList(
                    doorType = doorType,  // 특정 문 필터 적용
                    filter = "DEFAULT"
                )
                withContext(Dispatchers.Main) {
                    if (restaurantList.isNotEmpty()) {
                        navigateToSelectedDoorList(restaurantList, doorType)
                    } else {
                        showToast("필터 조건에 맞는 식당이 없습니다.")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("데이터 로드 중 오류가 발생했습니다.")
                }
                e.printStackTrace()
            }
        }
    }


    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            enableUserLocation()
        } else {
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
    // 현재 위치 추적 활성화
    private fun enableUserLocation() {
        Log.d("SIBAL", "실행됨")
        if (::naverMap.isInitialized && !isLocationFixed) {
            naverMap.locationTrackingMode = LocationTrackingMode.Follow
            naverMap.addOnLocationChangeListener { location ->
                if (!isLocationFixed) {
                    updateUserLocationMarker(location.latitude, location.longitude)
                    Log.d("LOCATION_UPDATE", "현재 위치: (${location.latitude}, ${location.longitude})")
                }
            }
            Log.d("LOCATION_TRACKING", "현재 위치 추적 시작")
        }
    }



    private fun initializeMapWithoutPermission() {
        val defaultPosition = LatLng(37.5834643, 127.0536246) // 서울시립대 정문
        val cameraPosition = CameraPosition(defaultPosition, 16.0)
        naverMap.cameraPosition = cameraPosition
        Log.d("MAP_INIT", "기본 위치로 초기화: ($defaultPosition)")
        loadRestaurants() // 식당 정보 불러오기
    }


    private fun updateUserLocationMarker(latitude: Double, longitude: Double) {
        if (userMarker == null) {
            userMarker = Marker().apply {
                position = LatLng(latitude, longitude)
                icon = OverlayImage.fromResource(R.drawable.ddong_playstore)
                width = 120
                height = 120
                captionText = "현재 위치"
                map = naverMap
            }
            Log.d("USER_MARKER", "사용자 마커 생성됨: ($latitude, $longitude)")
        } else {
            userMarker?.position = LatLng(latitude, longitude)
            Log.d("USER_MARKER", "사용자 마커 업데이트됨: ($latitude, $longitude)")
        }
        moveCameraToLocation(latitude, longitude)
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


    // DoorType 버튼 클릭 설정
    private fun setupFilterButtons() {
        binding.doorTypeButton1.setOnClickListener {
            filterMarkersByDoorType("정문", 37.5834643, 127.0536246, binding.doorTypeButton1)
        }

        binding.doorTypeButton2.setOnClickListener {
            filterMarkersByDoorType("쪽문", 37.5869791, 127.0564010, binding.doorTypeButton2)
        }

        binding.doorTypeButton3.setOnClickListener {
            filterMarkersByDoorType("후문", 37.5869320, 127.0606581, binding.doorTypeButton3)
        }

        binding.doorTypeButton4.setOnClickListener {
            filterMarkersByDoorType("남문", 37.5775540, 127.0578147, binding.doorTypeButton4)
        }
    }

    // 특정 DoorType에 해당하는 마커 필터링 및 카메라 이동
    private fun filterMarkersByDoorType(doorType: String, lat: Double, lon: Double, button: View) {
        if (selectedButton == button) {
            // 이미 선택된 버튼 다시 클릭 시 초기화
            resetToInitialState()
            showAllMarkers()  // 모든 마커 다시 표시
            showToast("모든 식당을 다시 표시합니다.")
        } else {
            // 버튼 상태 업데이트
            selectedButton?.isSelected = false
            button.isSelected = true
            selectedButton = button

            // 마커 필터링
            restaurantMarkers.forEach { marker ->
                marker.map = if (marker.tag == doorType) naverMap else null
            }

            // 카메라 이동
            moveCameraToLocation(lat, lon)
            showToast("$doorType 위치로 이동합니다.")
            isLocationFixed = true
            stopLocationTracking()
        }
    }

    // 모든 마커 다시 표시
    private fun showAllMarkers() {
        restaurantMarkers.forEach { marker ->
            marker.map = naverMap
        }
    }

    // 위치 추적 중지
    private fun stopLocationTracking() {
        naverMap.locationTrackingMode = LocationTrackingMode.NoFollow
        isLocationFixed = true
        Log.d("LOCATION_TRACKING", "위치 추적 중지됨")
    }


    private fun loadRestaurants() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val restaurants = RetrofitInstance.restaurantApi.getRestaurantList(null, "DEFAULT")
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
                    Toast.makeText(this@MainActivity, "데이터 로드 중 오류 발생 2", Toast.LENGTH_SHORT).show()
                }
                e.printStackTrace()
            }
        }
    }

    private fun addMarkersToMap(restaurantList: List<RestaurantListResponse>) {
        restaurantMarkers.clear()
        restaurantList.forEach { restaurant ->
            if (restaurant.latitude != 0.0 && restaurant.longitude != 0.0) {
                // 마커 추가
                val marker = Marker().apply {
                    position = LatLng(restaurant.latitude, restaurant.longitude)
                    map = naverMap
                    captionText = restaurant.name
                    tag = restaurant.doorType
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
                            tag = restaurant.doorType
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
    // 식당 상세 화면으로 이동
    private fun navigateToRestaurantDetail(restaurantInfo: RestaurantInfo) {
        val intent = Intent(this, RestaurantDetailActivity::class.java).apply {
            putExtra("restaurantId", restaurantInfo.id)
        }
        startActivity(intent)
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
    private fun setupSearch() {
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

            override fun onQueryTextChange(newText: String?): Boolean = false
        })
    }


    private fun moveCameraToLocation(latitude: Double, longitude: Double) {
        val cameraUpdate = CameraUpdate.scrollTo(LatLng(latitude, longitude))
        naverMap.moveCamera(cameraUpdate)
        Log.d("CAMERA_MOVE", "카메라 이동됨: ($latitude, $longitude)")
    }


    private fun searchRestaurants(keyword: String) {
        val searchDoorType = when {
            selectedDoorType != null -> selectedDoorType  // 필터 버튼 선택됨
            isNearbySearchEnabled && !isLocationFixed -> getClosestDoorType()  // 체크박스 선택 + 위치 추적 가능
            else -> null  // 기본 검색 (필터 없음)
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.restaurantApi.searchRestaurants(
                    keyword = keyword,
                    closestDoor = searchDoorType ?: "null"
                )

                withContext(Dispatchers.Main) {
                    if (response.isNotEmpty()) {
                        navigateToRestaurantList(response)
                    } else {
                        showToast("검색 결과가 없습니다.")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("검색 중 오류가 발생했습니다.")
                }
                e.printStackTrace()
            }
        }
    }

    /** 가장 가까운 문 계산
     * getClosestDoorType
     */
    private fun getClosestDoorType(): String? {
        val userLocation = locationSource.lastLocation ?: return null

        val doorLocations = mapOf(
            "정문" to LatLng(37.5834643, 127.0536246),
            "쪽문" to LatLng(37.5869791, 127.0564010),
            "후문" to LatLng(37.5869320, 127.0606581),
            "남문" to LatLng(37.5775540, 127.0578147)
        )

        val closestDoor = doorLocations.minByOrNull { (_, location) ->
            distanceBetween(userLocation.latitude, userLocation.longitude, location.latitude, location.longitude)
        }

        return closestDoor?.key
    }

    /** 거리 계산 유틸리티 함수
     * distanceBetween
     */
    private fun distanceBetween(
        lat1: Double, lon1: Double, lat2: Double, lon2: Double
    ): Float {
        val result = FloatArray(1)
        android.location.Location.distanceBetween(lat1, lon1, lat2, lon2, result)
        return result[0]
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
                restaurantMarkers.forEach { include(it.position) }
                userMarker?.let { include(it.position) }
            }.build()

            val cameraUpdate = CameraUpdate.fitBounds(bounds, 100)
            naverMap.moveCamera(cameraUpdate)
        }
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun checkLocationPermissionAndMoveCamera() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (::naverMap.isInitialized) {
                val lastLocation = locationSource.lastLocation
                if (lastLocation != null) {
                    moveCameraToLocation(lastLocation.latitude, lastLocation.longitude)
                    showToast("사용자 위치로 이동했습니다.")
                } else {
                    showToast("현재 위치를 가져올 수 없습니다.")
                }
            } else {
                Log.e("NAVER_MAP_ERROR", "NaverMap 초기화가 안됨")
            }
        } else {
            requestLocationPermission()
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    private fun resetToInitialState() {
        selectedButton?.isSelected = false
        selectedButton = null
        selectedDoorType = null
        isLocationFixed = false

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            checkLocationPermissionAndMoveCamera()
        } else {
            // 위치 권한이 없을 경우 정문으로 이동
            moveCameraToLocation(37.5834643, 127.0536246)
            showToast("위치 권한이 없어 정문 위치로 이동합니다.")
        }
    }

    private fun updateButtonSelection(newButton: View, doorType: String, lat: Double, lon: Double) {
        if (selectedButton == newButton) {
            // 이미 선택된 버튼을 다시 누르면 초기 상태로 복귀
            resetToInitialState()
        } else {
            // 버튼 선택 상태 업데이트
            selectedButton?.isSelected = false
            newButton.isSelected = true
            selectedButton = newButton
            selectedDoorType = doorType
            isLocationFixed = true

            // 해당 위치로 이동
            moveCameraToLocation(lat, lon)
            showToast("$doorType 위치로 이동합니다.")
        }
    }

    /**
     * 검색창 글씨체는 따로 함수정의해서 색상 변경
     */
    private fun customizeSearchView() {
        try {
            // SearchView의 AutoCompleteTextView 가져오기
            val searchAutoComplete = binding.svSearch.javaClass
                .getDeclaredField("mSearchSrcTextView")
                .apply { isAccessible = true }
                .get(binding.svSearch) as? android.widget.AutoCompleteTextView

            searchAutoComplete?.apply {
                setHintTextColor(resources.getColor(R.color.black, null))  // 힌트 텍스트 색상 설정
                setTextColor(resources.getColor(R.color.black, null))     // 입력 텍스트 색상 설정
                textSize = 16f                                            // 텍스트 크기 설정
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * (iv_search) 검색 버튼 눌러질 떄 이벤트 처리
     */
    private fun setupClickListeners() {
        binding.ivSearch.setOnClickListener {
            val query = binding.svSearch.query.toString().trim()
            if (query.isNotBlank()) {
                searchRestaurants(query)
            } else {
                Toast.makeText(this, "검색어를 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 필터를 통한 식당 로딩 (by doortype)
     */



    /**
     * 필터 설정한 후 "목록보기" 버튼 누를 시 필터(doorType)없는 리스트 액티비티 이동
     */
    private fun navigateToSelectedDoorList(restaurantList: List<RestaurantListResponse>) {
        val intent = Intent(this, SelectedDoorActivity::class.java).apply {
            putParcelableArrayListExtra("restaurantList", ArrayList(restaurantList))
            putExtra("doorType", selectedDoorType)
        }
        startActivity(intent)
    }

    // 특정 DoorType 필터 식당 목록으로 이동하는 함수
    private fun navigateToSelectedDoorList(
        restaurantList: List<RestaurantListResponse>,
        doorType: String
    ) {
        val intent = Intent(this, SelectedDoorActivity::class.java).apply {
            putParcelableArrayListExtra("restaurantList", ArrayList(restaurantList))
            putExtra("doorType", doorType)
        }
        startActivity(intent)
    }

    // 전체 목록 보기로 이동 (정문 기본 선택)
    private fun navigateToRestaurantList(restaurantList: List<RestaurantListResponse>) {
        val intent = Intent(this, RestaurantListActivity::class.java).apply {
            putParcelableArrayListExtra("restaurantList", ArrayList(restaurantList))
            putExtra("defaultDoorType", "정문") // 정문 기본 선택
        }
        startActivity(intent)
    }
    // 로그인 화면으로 이동
    private fun navigateToLoginActivity() {
        val intent = Intent(this, StartActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }





}






