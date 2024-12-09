package com.example.uosense

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource

/**
 * MainActivity에서 쓰는 일부 함수 리팩토링하기 위해서
 */
object AppUtils {
    

    lateinit var naverMap: NaverMap
    var userMarker: Marker? = null
    val restaurantMarkers = mutableListOf<Marker>()
    lateinit var locationSource: FusedLocationSource


    /**
     * 현재 위치 마커 업데이트
     */
    fun updateUserLocationMarker(latitude: Double, longitude: Double) {
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

    /**
     * 카메라를 특정 위치로 이동
     */
    fun moveCameraToLocation(latitude: Double, longitude: Double) {
        val cameraUpdate = CameraUpdate.scrollTo(LatLng(latitude, longitude))
        naverMap.moveCamera(cameraUpdate)
        Log.d("CAMERA_MOVE", "카메라 이동됨: ($latitude, $longitude)")
    }

    /**
     * 카메라를 모든 마커가 보이도록 이동
     */
    fun moveCameraToFitAllMarkers() {
        if (restaurantMarkers.isNotEmpty()) {
            val bounds = LatLngBounds.Builder().apply {
                restaurantMarkers.forEach { include(it.position) }
                userMarker?.let { include(it.position) }
            }.build()

            val cameraUpdate = CameraUpdate.fitBounds(bounds, 100)
            naverMap.moveCamera(cameraUpdate)
        }
    }

    /**
     * 가장 가까운 문을 계산하여 반환
     */
    fun getClosestDoorType(userLat: Double, userLon: Double): String? {
        val doorLocations = mapOf(
            "정문" to LatLng(37.5834643, 127.0536246),
            "쪽문" to LatLng(37.5869791, 127.0564010),
            "후문" to LatLng(37.5869320, 127.0606581),
            "남문" to LatLng(37.5775540, 127.0578147)
        )

        val closestDoor = doorLocations.minByOrNull { (_, location) ->
            distanceBetween(userLat, userLon, location.latitude, location.longitude)
        }

        return closestDoor?.key
    }

    /**
     * 두 좌표 간 거리 계산
     */
    fun distanceBetween(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        val result = FloatArray(1)
        android.location.Location.distanceBetween(lat1, lon1, lat2, lon2, result)
        return result[0]
    }

    /**
     * Toast 메시지 출력
     */
    fun showToast(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, message, duration).show()
    }
}
