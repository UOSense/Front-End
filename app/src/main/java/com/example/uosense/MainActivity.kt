package com.example.uosense

import com.example.uosense.AppUtils
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
import com.example.uosense.AppUtils.getClosestDoorType
import com.example.uosense.AppUtils.showToast
import com.example.uosense.models.CategoryType
import com.example.uosense.models.DoorType
import com.example.uosense.models.SubDescriptionType
import com.example.uosense.network.RetrofitInstance.restaurantApi
import com.naver.maps.map.overlay.OverlayImage

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.naver.maps.map.OnMapReadyCallback
import java.net.URLDecoder
import java.sql.Types.NULL
import kotlin.math.log


class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    //위치 관련 변수
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    //지도 관련 변수
    private lateinit var binding: ActivityMainBinding
    private lateinit var naverMap: NaverMap
    private lateinit var locationPermissionLauncher: ActivityResultLauncher<Array<String>>
    private var userMarker: Marker? = null
    private val restaurantMarkers = mutableListOf<Marker>()
    private lateinit var locationSource: FusedLocationSource
    // 위치 추적 모드 관리 변수
    private var isLocationFixed = false

    private lateinit var tokenManager: TokenManager

    private lateinit var restaurantList: MutableList<RestaurantListResponse>

    private var currentPage = 1
    private val pageSize = 10





    //검색을 위해서 새로운 변수
    private var selectedDoorType: String? = null
    private var isNearbySearchEnabled = false

    /**
     * 초기 상태 관리 및 버튼 토글 기능 추가
     */
    private var selectedButton: View? = null


    /**
     * 다시 돌아왔을 때 마커 재생성 -> 곧 위도 경도 저장해서 로딩 시간 줄일 것
     */
    override fun onResume() {
        super.onResume()
        if (::naverMap.isInitialized) {
            clearMarkers()  // 마커 초기화
            loadRestaurants()  // 식당 목록 다시 로딩
        }
    }

    /**
     * onResume()을 위한 함수, 로그 확인 완료
     */
    private fun clearMarkers() {
        restaurantMarkers.forEach { it.map = null }
        restaurantMarkers.clear()
        Log.d("MARKERS_RESET", "모든 마커 초기화")
    }

    override fun onMapReady(map: NaverMap) {
        naverMap = map
        naverMap.locationSource = locationSource

        // 기본 위치로 초기화 (서울시립대 정문)
        val defaultPosition = LatLng(37.5834643, 127.0536246)
        val cameraPosition = CameraPosition(defaultPosition, 16.0)
        naverMap.cameraPosition = cameraPosition

        // 카메라 이동 시 수동 이동으로 간주
        naverMap.addOnCameraChangeListener { reason, animated ->
            if (!animated) stopLocationTracking()
        }

        // 위치 권한 요청
        requestLocationPermission()
        loadRestaurants()

        Log.d("NAVER_MAP_INIT", "초기 카메라 위치 설정 완료: $defaultPosition")
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TokenManager 초기화
        tokenManager = TokenManager(this)

        // 뷰 바인딩 초기화
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 네이버 지도 초기화
        NaverMapSdk.getInstance(this).client = NaverMapSdk.NaverCloudPlatformClient("s78aa7asq0")

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance().also {
                supportFragmentManager.beginTransaction().add(R.id.map, it).commit()
            }

        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        mapFragment.getMapAsync(this)

        // 리프레시 토큰 검증
        val refreshToken = tokenManager.getRefreshToken()
        if (refreshToken.isNullOrEmpty()) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show()
            navigateToLoginActivity() // 로그인 화면으로 이동
            return
        }



        /**
         * 어플 들어올 때마다 위치 권한 설정되어 있으면 문제없이 넘어가야함
         * 만약 설정되어 있지 않으면 사용자 위치 권한 요청 TOAST 출력
         */
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    Log.d("LOCATION_UPDATE", "위치 업데이트: ${location.latitude}, ${location.longitude}")
                }
            }
        }

        setupLocationPermissionLauncher()



        // 체크박스 리스너 설정
        binding.chkNearby.setOnCheckedChangeListener { _, isChecked ->
            isNearbySearchEnabled = isChecked
            if (isChecked) {
                // 가장 가까운 문 계산 및 필터 적용
                getUserLocationAndSearchRestaurants()
            } else {
                // 체크박스 해제 시 전체 식당 로딩
                selectedDoorType = null
            }
        }

        binding.doorTypeButton1.setOnClickListener {
            selectedDoorType = "FRONT"
            isLocationFixed = true
            moveCameraToLocation(37.5834643, 127.0536246)
            showToast(this, "정문 위치로 이동합니다.")
        }

        binding.doorTypeButton2.setOnClickListener {
            selectedDoorType = "SIDE"
            isLocationFixed = true
            moveCameraToLocation(37.5869791, 127.0564010)
            showToast(this,"쪽문 위치로 이동합니다.")
        }

        binding.doorTypeButton3.setOnClickListener {
            selectedDoorType = "BACK"
            isLocationFixed = true
            moveCameraToLocation(37.5869320, 127.0606581)
            showToast(this,"후문 위치로 이동합니다.")
        }

        binding.doorTypeButton4.setOnClickListener {
            selectedDoorType = "SOUTH"
            isLocationFixed = true
            moveCameraToLocation(37.5775540, 127.0578147)
            showToast(this,"남문 위치로 이동합니다.")
        }



        // 내 위치 버튼 클릭 시 위치 추적 재시작
        binding.btnUserLocation.setOnClickListener {
            checkLocationPermissionAndMoveCamera()
            enableUserLocation()
            isLocationFixed = false
        }

        setupSearch()

        setupFilterButtons()

        setupClickListeners()

        customizeSearchView()


        // "목록 보기" 버튼 클릭 시 동작 수정
        // 수정된 CategoryBtn 클릭 리스너
        binding.categoryBtn.setOnClickListener {
            if (selectedDoorType.isNullOrBlank()) {
                loadAllRestaurants()  // 선택된 문이 없으면 전체 식당 로딩
            } else {
                binding.categoryBtn.isEnabled = false  // 버튼 비활성화
                val doorTypeForApi = mapDoorTypeForApi(selectedDoorType!!)
                loadRestaurantsByFilter(doorTypeForApi)
            }
        }

        binding.userProfileBtn.setOnClickListener {
            val intent = Intent(this, MyPageActivity::class.java)
            startActivity(intent)
        }


    }

    // doorType을 API에서 사용하는 값으로 매핑하는 함수
    private fun mapDoorTypeForApi(doorType: String): String {
        return when (doorType) {
            "정문" -> "FRONT"
            "남문" -> "SOUTH"
            "쪽문" -> "SIDE"
            "후문" -> "BACK"
            else -> "NULL"  // 기본값 처리
        }
    }
    // CategoryType 매핑 함수
    private fun mapCategoryForApi(category: String): String {
        return when (category.uppercase()) {
            "한식" -> "KOREAN"
            "중식" -> "CHINESE"
            "일식" -> "JAPANESE"
            "양식" -> "WESTERN"
            "기타" -> "OTHER"
            else -> "NULL"
        }
    }

    // SubDescriptionType 매핑 함수
    private fun mapSubDescription(subDescription: String): String {
        return when (subDescription.uppercase()) {
            "술집" -> "BAR"
            "카페" -> "CAFE"
            "식당" -> "RESTAURANT"
            else -> "NULL"
        }
    }


    // 위치 권한 요청 초기화
    private fun setupLocationPermissionLauncher() {
        locationPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            ) {
                    enableUserLocation()
                } else {
                    initializeMapWithoutPermission()
                }
            }
        }




    // 전체 식당 로딩 함수
    private fun loadAllRestaurants() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = restaurantApi.getRestaurantList(
                    filter = "DEFAULT"
                )
                withContext(Dispatchers.Main) {
                    response.body()?.let {
                        if (it.isNotEmpty()) {
                            navigateToRestaurantList(it)
                        } else {
                            showToast(this@MainActivity, "식당 정보가 없습니다.", Toast.LENGTH_SHORT)
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast(this@MainActivity, "API 호출 오류 발생", Toast.LENGTH_SHORT)
                }
                e.printStackTrace()
            }
        }
    }

    // 필터 누름에 따라 식당 리스트 반환 1. 검색 2. 목록 보기
    private fun loadRestaurantsByFilter(doorType: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("API_CALL", "doorType: $doorType")  // 로그 추가
                val response = restaurantApi.getRestaurantList(
                    doorType = doorType,
                    filter = "DEFAULT"
                )
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val restaurantList = response.body()!!
                        if (restaurantList.isNotEmpty()) {
                            navigateToSelectedDoorList(restaurantList, doorType)
                        } else {
                            showToast(this@MainActivity, "선택한 문 주변 식당이 없습니다.")
                            Log.d("API_RESULT", "빈 목록 반환됨")
                        }
                    } else {
                        showToast(this@MainActivity, "API 응답 오류 발생")
                        Log.e("API_ERROR", "응답 오류: ${response.errorBody()?.string()}")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    showToast(this@MainActivity, "API 호출 오류 발생: ${e.message}")
                }
            } finally {
                withContext(Dispatchers.Main) {
                    binding.categoryBtn.isEnabled = true  // 버튼 활성화
                }
            }
        }
    }


    // 위치 권한 접근 요청
    private fun requestLocationPermission() {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
    // 현재 위치 추적 활성화 -> 연결해서 확인해보기
    private fun enableUserLocation() {
        if (::naverMap.isInitialized && !isLocationFixed) {
            naverMap.locationTrackingMode = LocationTrackingMode.Follow
            naverMap.addOnLocationChangeListener { location ->
                if (!isLocationFixed) {
                    updateUserLocationMarker(location.latitude, location.longitude)
                }
            }
        } else {
            Log.e("NAVER_MAP_ERROR", "NaverMap이 초기화되지 않음")
        }
    }




    //위치 권한 없을 때 초기 위치 설정
    private fun initializeMapWithoutPermission() {
        val defaultPosition = LatLng(37.5834643, 127.0536246) // 서울시립대 정문
        val cameraPosition = CameraPosition(defaultPosition, 16.0)
        naverMap.cameraPosition = cameraPosition
        Log.d("MAP_INIT", "기본 위치로 초기화: ($defaultPosition)")
        loadRestaurants()
    }

    // 사용자 위치 마커 업데이트
    private fun updateUserLocationMarker(latitude: Double, longitude: Double) {
        userMarker?.position = LatLng(latitude, longitude)
        Log.d("USER_MARKER", "사용자 마커 업데이트됨: ($latitude, $longitude)")
        moveCameraToLocation(latitude, longitude)
    }


    private fun updateRestaurantCoordinatesToServer(
        restaurantId: Int, latitude: Double, longitude: Double
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 기존 레스토랑 정보 가져오기
                val existingResponse = restaurantApi.getRestaurantById(restaurantId)

                if (!existingResponse.isSuccessful || existingResponse.body() == null) {
                    Log.e("SERVER_FETCH_ERROR", "기존 정보 가져오기 실패: ${existingResponse.errorBody()?.string()}")
                    return@launch
                }

                val existingRestaurant = existingResponse.body()!!

                // 위치 업데이트 요청 생성 (기존 데이터 유지)
                val updateRequest = RestaurantRequest(
                    id = restaurantId,
                    name = existingRestaurant.name.ifBlank { "Unknown" },
                    doorType = mapDoorTypeForApi(existingRestaurant.doorType ?: "NULL"),
                    latitude = latitude,
                    longitude = longitude,
                    address = existingRestaurant.address ?: "",
                    phoneNumber = existingRestaurant.phoneNumber ?: "",
                    category = mapCategoryForApi(existingRestaurant.category ?:"음식점") ,
                    subDescription = mapSubDescription(existingRestaurant.subDescription ?:"기타") ,
                    description = existingRestaurant.description ?: ""
                )

                // 위치 업데이트 요청 전송
                val updateResponse = restaurantApi.updateRestaurantLocation(updateRequest)

                withContext(Dispatchers.Main) {
                    if (updateResponse.isSuccessful) {
                        Log.d("SERVER_UPDATE", "레스토랑 위치 업데이트 성공: ($latitude, $longitude)")
                        showToast(this@MainActivity, "위치 업데이트 성공!")
                    } else {
                        val errorMessage = updateResponse.errorBody()?.string()
                        Log.e("SERVER_UPDATE_ERROR", "업데이트 실패: $errorMessage")
                        showToast(this@MainActivity, "업데이트 실패: $errorMessage")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast(this@MainActivity, "위치 업데이트 중 오류 발생: ${e.message}")
                }
                Log.e("SERVER_ERROR", "위치 업데이트 중 예외 발생", e)
            }
        }
    }






    // 도로명 주소 -> 위도, 경도 변환
    private fun getLatLngFromAddress(restaurant: RestaurantListResponse, callback: (Double?, Double?) -> Unit) {
        val client = OkHttpClient()
        val url = "https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode?query=${restaurant.address}"

        // ID, KEY 절대 수정 X , SECRET 사용 가능
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

                    // 서버 업데이트 호출
                    updateRestaurantCoordinatesToServer(restaurant.id, latitude, longitude)


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

    // DoorType에 해당하는 마커 필터링 및 카메라 이동
    private fun filterMarkersByDoorType(doorType: String, lat: Double, lon: Double, button: View) {
        // 선택된 버튼이 이미 눌렸을 경우 전체 마커를 보여주는 로직
        if (selectedButton == button) {
            resetToInitialState()
            showAllMarkers()  // 모든 마커를 다시 표시
        } else {
            // 다른 버튼을 클릭했을 때 마커를 필터링하여 보여주는 로직
            selectedButton?.isSelected = false
            button.isSelected = true
            selectedButton = button
            selectedDoorType = doorType

            // 선택된 doorType에 해당하는 마커만 보이게
            restaurantMarkers.forEach { marker ->
                if (marker.tag == doorType) {
                    marker.isVisible = true // 해당 doorType에 맞는 마커만 지도에 표시
                } else {
                    marker.isVisible = false // 다른 doorType의 마커는 숨기기
                }
            }

            moveCameraToLocation(lat, lon)  // 해당 위치로 카메라 이동
            isLocationFixed = true
            stopLocationTracking()
        }
    }

    // 모든 마커 다시 표시
    private fun showAllMarkers() {
        restaurantMarkers.forEach { marker ->
            marker.isVisible = true  // 모든 마커를 지도에 다시 표시
        }
    }


    // 위치 추적 중지
    private fun stopLocationTracking() {
        if (::naverMap.isInitialized) {
            naverMap.locationTrackingMode = LocationTrackingMode.NoFollow
            isLocationFixed = true
            Log.d("LOCATION_TRACKING", "위치 추적 중지됨")
        }
    }

    private fun loadRestaurants() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // API 호출
                val response = restaurantApi.getRestaurantList(
                    doorType = null,
                    filter = "DEFAULT"
                )

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                        restaurantList = response.body()!!.toMutableList()

                        // DoorType 업데이트
                        addMarkersToMap(restaurantList)  // 마커 추가
                    } else {
                        showToast(
                            this@MainActivity,
                            "식당 정보가 없습니다.",
                            Toast.LENGTH_SHORT
                        )
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()  // 에러 로그 출력

                withContext(Dispatchers.Main) {
                    showToast(
                        this@MainActivity,
                        "API 호출 오류 발생: ${e.message}",
                        Toast.LENGTH_SHORT
                    )
                }
            }
        }
    }






    // 마커 추가(MAP)
    private fun addMarkersToMap(restaurantList: List<RestaurantListResponse>) {
        restaurantMarkers.clear()
        restaurantList.forEach { restaurant ->
            if (restaurant.latitude != 0.0 && restaurant.longitude != 0.0) {
                val marker = Marker().apply {
                    position = LatLng(restaurant.latitude, restaurant.longitude)
                    map = naverMap
                    captionText = restaurant.name
                    tag = restaurant.doorType
                    isVisible = true  // 초기에는 모든 마커를 표시하도록 설정
                }

                marker.setOnClickListener {
                    // 마커 클릭 시 RestaurantDetail 액티비티로 id 넘기면서 이동
                    fetchRestaurantDetails(restaurant.id)
                    true
                }
                restaurantMarkers.add(marker)
            } else {
                // (위도 경도 0, 0)대부분 해당, Geocoding API 호출
                getLatLngFromAddress(restaurant) { lat, lng ->
                    if (lat != null && lng != null) {
                        val marker = Marker().apply {
                            position = LatLng(lat, lng)
                            map = naverMap
                            captionText = restaurant.name
                            tag = restaurant.doorType
                            isVisible = true
                        }

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

    // 마커 클릭 -> fetchRestaurantDetails -> 특정 식당 정보 조회
    private fun fetchRestaurantDetails(restaurantId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.restaurantApi.getRestaurantById(restaurantId)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        navigateToRestaurantDetail(response.body()!!)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                }
            }
        }
    }
    // 식당 상세 화면으로 이동
    private fun navigateToRestaurantDetail(restaurantInfo: RestaurantInfo) {
        val intent = Intent(this, RestaurantDetailActivity::class.java).apply {
            putExtra("restaurantId", restaurantInfo.id)
        }
        startActivity(intent)
    }
    // 검색 설정 (로직 X)
    private fun setupSearch() {
        binding.svSearch.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val trimmedQuery = query?.trim()
                if (!trimmedQuery.isNullOrBlank()) {
                    searchRestaurants(trimmedQuery)
                } else {
                    showToast(this@MainActivity, "검색어를 입력해주세요.")
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean = false
        })

    }
    // 사용자의 위치 가져오기 & 가장 가까운 문 계산
    private fun getUserLocationAndSearchRestaurants() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val closestDoorType = getClosestDoorType(it.latitude, it.longitude)
                    if (!closestDoorType.isNullOrEmpty()) {
                        selectedDoorType = closestDoorType
                    } else {
                        showToast(this, "가장 가까운 문을 찾을 수 없습니다.")
                    }
                }
            }
        } else {
            requestLocationPermission()  // 권한 요청
        }
    }


    // 카메라 위치에 맞게 조정
    private fun moveCameraToLocation(latitude: Double, longitude: Double) {
        val cameraUpdate = CameraUpdate.scrollTo(LatLng(latitude, longitude))
        naverMap.moveCamera(cameraUpdate)
        Log.d("CAMERA_MOVE", "카메라 이동됨: ($latitude, $longitude)")
    }

    // 검색 API 호출 
    private fun searchRestaurants(keyword: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val apiDoorType = if (!selectedDoorType.isNullOrEmpty()) {
                    mapDoorTypeForApi(selectedDoorType!!)
                }else {
                    null
                }
                val response = restaurantApi.searchRestaurants(
                    keyword = keyword,
                    doorType = apiDoorType
                )
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null && response.body()!!.isNotEmpty()) {
                        if (isNearbySearchEnabled && selectedDoorType != null) {
                            // 체크박스 눌려 있을 때 검색 시 특정 문으로 이동
                            navigateToSelectedDoorList(response.body()!!, mapDoorTypeForApi(selectedDoorType!!))
                        } else {
                            // 체크박스 해제 시 전체 목록 이동
                            navigateToRestaurantList(response.body()!!)
                        }
                    } else {
                        showToast(this@MainActivity, "검색 결과가 없습니다.")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {

                }
            }
        }
    }


    /** 가장 가까운 문 계산 -> AppUtils
     * getClosestDoorType
     */


    /** 거리 계산 유틸리티 함수 -> AppUtils
     * distanceBetween
     */

    // getLan 통해서 위도 경도 획득 -> 업데이트 시 필요 함수
    private fun updateRestaurantCoordinates() {
        restaurantList.forEach { restaurant ->
            if (restaurant.longitude == 0.0 && restaurant.latitude == 0.0) {
                AppUtils.getLatLngFromAddress(restaurant.address) { lat, lng ->
                    if (lat != null && lng != null) {
                        restaurant.latitude = lat
                        restaurant.longitude = lng
                    }
                }
            }
        }
    }

    // 모든 마커에 맞춰서 카메라 이동
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


// 위치 권한 확인 및 카메라 이동
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
                } else {

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
    // 초기 맵 위치 설정
    private fun resetToInitialState() {
        selectedButton?.isSelected = false
        selectedButton = null
        isLocationFixed = false
        selectedDoorType = null

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
            showToast(this@MainActivity,"위치 권한이 없어 정문 위치로 이동합니다.")
        }
    }


    /**
     * 검색창 글씨체는 따로 함수정의해서 색상 변경
     * xml 파일 내에서 수정 X
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
            putExtra("defaultDoorType", "FRONT") // 정문 기본 선택
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