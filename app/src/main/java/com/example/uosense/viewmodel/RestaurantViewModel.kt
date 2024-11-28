package com.example.uosense.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uosense.models.Restaurant
import com.example.uosense.models.BusinessDay
import com.example.uosense.models.RestaurantInfo
import com.example.uosense.models.RestaurantListResponse
import com.example.uosense.network.RetrofitInstance
import kotlinx.coroutines.launch

class RestaurantViewModel : ViewModel() {
    private val api = RetrofitInstance.api

    val restaurantList = MutableLiveData<List<RestaurantListResponse>>()
    val restaurantInfo = MutableLiveData<Restaurant>()

    // 공통: Mock BusinessDays 생성
    private fun createMockBusinessDays(restaurantId: Int): List<BusinessDay> {
        return listOf(
            BusinessDay(
                id = 1,
                restaurant_id = restaurantId,
                day_of_week = "Monday",
                have_break_time = true,
                start_break_time = "15:00",
                stop_break_time = "17:00",
                opening_time = "09:00",
                closing_time = "22:00",
                is_holiday = false
            ),
            BusinessDay(
                id = 2,
                restaurant_id = restaurantId,
                day_of_week = "Tuesday",
                have_break_time = false,
                start_break_time = null,
                stop_break_time = null,
                opening_time = "09:00",
                closing_time = "22:00",
                is_holiday = false
            )
        )
    }

    // 공통: Mock Restaurant 생성
    private fun createMockRestaurant(restaurantId: Int): Restaurant {
        return Restaurant(
            id = restaurantId,
            name = "Test Restaurant",
            door_type = "정문",
            longitude = 127.0340,
            latitude = 37.5665,
            address = "서울시 종로구 대학로",
            phone_number = "010-1234-5678",
            rating = 4.5,
            category = "한식",
            sub_description = "음식점",
            description = "맛있는 한식 전문점 $restaurantId",
            review_count = 10,
            bookmark_count = 5,
            businessDays = createMockBusinessDays(restaurantId)
        )
    }

    // 모든 레스토랑 목록 가져오기
    fun fetchAllRestaurants(doorType: String?, category: String?) {
        viewModelScope.launch {
            try {
                val response = api.getAllRestaurants(doorType, category)
                restaurantList.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // 특정 ID의 레스토랑 가져오기 (Mock or API)
    fun fetchRestaurantById(restaurantId: Int) {
        viewModelScope.launch {
            try {
                // Mock 데이터 활용 (API 호출이 아닌 경우)
                val mockRestaurant = createMockRestaurant(restaurantId)
                restaurantInfo.postValue(mockRestaurant)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Mock 데이터를 레스토랑 목록으로 변환
    fun fetchMockRestaurants() {
        val mockRestaurant = createMockRestaurant(1)
        val restaurantListResponse = RestaurantListResponse(
            id = mockRestaurant.id,
            name = mockRestaurant.name,
            address = mockRestaurant.address,
            category = mockRestaurant.category,
            door_type = mockRestaurant.door_type,
            phone_number = mockRestaurant.phone_number,
            rating = mockRestaurant.rating
        )
        restaurantList.postValue(listOf(restaurantListResponse))
    }
}
