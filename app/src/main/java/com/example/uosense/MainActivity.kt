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
import com.example.uosense.AppUtils.showToast
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
        setContentView(binding.root)

        binding.chkNearby.setOnCheckedChangeListener { _, isChecked ->
            isNearbySearchEnabled = isChecked
        }

        binding.doorTypeButton1.setOnClickListener {
            selectedDoorType = "정문"
            isLocationFixed = true
            moveCameraToLocation(37.5834643, 127.0536246)
            showToast(this, "정문 위치로 이동합니다.")
        }

        binding.doorTypeButton2.setOnClickListener {
            selectedDoorType = "쪽문"
            isLocationFixed = true
            moveCameraToLocation(37.5869791, 127.0564010)
            showToast(this,"쪽문 위치로 이동합니다.")
        }

        binding.doorTypeButton3.setOnClickListener {
            selectedDoorType = "후문"
            isLocationFixed = true
            moveCameraToLocation(37.5869320, 127.0606581)
            showToast(this,"후문 위치로 이동합니다.")
        }

        binding.doorTypeButton4.setOnClickListener {
            selectedDoorType = "남문"
            isLocationFixed = true
            moveCameraToLocation(37.5775540, 127.0578147)
            showToast(this,"남문 위치로 이동합니다.")
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

            /**
             * 카메라 이동 시 해당 위치에 고정 (위치 추적 X)
             */
            naverMap.addOnCameraChangeListener { reason, animated ->
                if (!animated) {  // 애니메이션이 아니면 수동 이동으로 간주
                    stopLocationTracking()
                }
            }


            initializeMapWithoutPermission()
            requestLocationPermission()
        }

        binding.btnUserLocation.setOnClickListener {
            enableUserLocation()
            isLocationFixed = false
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

            /**
             * 실시간 검색 로직 구현 시
             */
            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

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
                val response = RetrofitInstance.restaurantApi.getRestaurantList(
                    doorType = null,
                    filter = "DEFAULT"
                )
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null && response.body()!!.isNotEmpty()) {
                        navigateToRestaurantList(response.body()!!)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadRestaurantsByFilter(doorType: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.restaurantApi.getRestaurantList(
                    doorType = doorType,
                    filter = "DEFAULT"
                )
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null && response.body()!!.isNotEmpty()) {
                        navigateToSelectedDoorList(response.body()!!, doorType)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
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



    // 도로명 주소 -> 위도, 경도 변환
    private fun getLatLngFromAddress(address: String, callback: (Double?, Double?) -> Unit) {
        val client = OkHttpClient()
        val url = "https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode?query=${address}"
        println(url)
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
        if (selectedButton == button) {
            resetToInitialState()
            showAllMarkers()

        } else {
            selectedButton?.isSelected = false
            button.isSelected = true
            selectedButton = button

            restaurantMarkers.forEach { marker ->
                marker.map = if (marker.tag == doorType) naverMap else null
            }
            moveCameraToLocation(lat, lon)

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
        if (::naverMap.isInitialized) {
            naverMap.locationTrackingMode = LocationTrackingMode.NoFollow
            isLocationFixed = true
            Log.d("LOCATION_TRACKING", "위치 추적 중지됨")
        }
    }



    private fun loadRestaurants() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.restaurantApi.getRestaurantList(null, "DEFAULT")
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    withContext(Dispatchers.Main) {
                        addMarkersToMap(response.body()!!)
                    }
                } else {
                    showToast(this@MainActivity,"식당 정보가 없습니다.", Toast.LENGTH_SHORT)
                }
            } catch (e: Exception) {
                    //필요시 TOAST 메시지 출력
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
                }

                marker.setOnClickListener {
                    // 마커 클릭 시 RestaurantDetail 액티비티로 id 넘기면서 이동
                    fetchRestaurantDetails(restaurant.id)
                    true
                }
                restaurantMarkers.add(marker)
            } else {
                // (위도 경도 0, 0)대부분 해당, Geocoding API 호출
                getLatLngFromAddress(restaurant.address) { lat, lng ->
                    if (lat != null && lng != null) {
                        val marker = Marker().apply {
                            position = LatLng(lat, lng)
                            map = naverMap
                            captionText = restaurant.name
                            tag = restaurant.doorType
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
            // 검색하면서 변할 때 어떻게 처리할 것인지?
            override fun onQueryTextChange(newText: String?): Boolean = false
        })
    }


    private fun moveCameraToLocation(latitude: Double, longitude: Double) {
        val cameraUpdate = CameraUpdate.scrollTo(LatLng(latitude, longitude))
        naverMap.moveCamera(cameraUpdate)
        Log.d("CAMERA_MOVE", "카메라 이동됨: ($latitude, $longitude)")
    }


    private fun searchRestaurants(keyword: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.restaurantApi.searchRestaurants(
                    keyword = keyword,
                    doorType = selectedDoorType
                )
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null && response.body()!!.isNotEmpty()) {
                        navigateToRestaurantList(response.body()!!)
                    } else {

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
                    showToast(this,"사용자 위치로 이동했습니다.")
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