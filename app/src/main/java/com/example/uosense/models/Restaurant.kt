package com.example.uosense.models

import android.os.Parcelable
import android.os.Parcel


data class BusinessDay(
    val id: Int, // 고유 ID
    val restaurant_id: Int, // Restaurant의 외래 키
    val day_of_week: String, // 요일 (enum 값: 'Monday', 'Tuesday', ...)
    val have_break_time: Boolean, // 쉬는 시간 여부
    val start_break_time: String?, // 쉬는 시간 시작 (nullable)
    val stop_break_time: String?, // 쉬는 시간 종료 (nullable)
    val opening_time: String, // 영업 시작 시간
    val closing_time: String, // 영업 종료 시간
    val is_holiday: Boolean // 휴일 여부
)


data class Restaurant(
    val id: Int, // 식당 고유 ID
    val name: String, // 식당 이름
    val door_type: String, // 출입구 유형 (enum 값: '정문', '쪽문', '후문')
    val longitude: Double, // 경도
    val latitude: Double, // 위도
    val address: String, // 주소
    val phone_number: String?, // 전화번호 (nullable)
    val rating: Double, // 평점
    val category: String, // 카테고리 (enum 값: '한식', '중식', '일식', '양식', '기타')
    val sub_description: String, // 부가 설명 (enum 값: '술집', '음식점', '카페')
    val description: String?, // 상세 설명 (nullable)
    val review_count: Int, // 리뷰 수
    val bookmark_count: Int, // 즐겨찾기 수
    val businessDays: List<BusinessDay> // 1:N 관계로 연결된 BusinessDay 리스트
)

data class RestaurantListResponse(
    val imageResourceId: Int,
    val id: Int,
    val name: String,
    val address: String,
    val category: String,
    val door_type: String?, // 추가
    val phone_number: String?, // 추가
    val rating: Double,
    val review_count: Int
) {

}


data class RestaurantInfo(
    val id: Int,
    val name: String,
    val address: String,
    val description: String?,
    val category: String, // 반드시 String 타입이어야 함
    val menu: List<MenuResponse>,
    val businessDays: List<BusinessDay>
)


data class RestaurantRequest(
    val id: Int,
    val name: String,
    val address: String,
    val description: String
)

