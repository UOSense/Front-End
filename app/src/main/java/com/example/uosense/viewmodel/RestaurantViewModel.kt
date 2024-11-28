package com.example.uosense.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uosense.models.Restaurant
import com.example.uosense.models.BusinessDay
import com.example.uosense.network.RetrofitInstance
import kotlinx.coroutines.launch

class RestaurantViewModel : ViewModel() {

    private val _restaurants = MutableLiveData<List<Restaurant>>()
    val restaurants: LiveData<List<Restaurant>> get() = _restaurants

    private val _selectedRestaurant = MutableLiveData<Restaurant>()
    val selectedRestaurant: LiveData<Restaurant> get() = _selectedRestaurant

    private val api = RetrofitInstance.api

    // 모든 식당 정보 가져오기
    fun fetchAllRestaurants() {
        viewModelScope.launch {
            try {
                val response = api.getAllRestaurants()
                _restaurants.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace() // 에러 로그
            }
        }
    }

    // 특정 식당 정보 가져오기
    fun fetchRestaurantById(restaurantId: Int) {
        viewModelScope.launch {
            try {
                val response = api.getRestaurant(restaurantId)
                _selectedRestaurant.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace() // 에러 로그
            }
        }
    }

    // Mock Data (테스트용)
    fun fetchMockRestaurants() {
        val exampleBusinessDays = listOf(
            BusinessDay(
                id = 1,
                restaurant_id = 1,
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
                restaurant_id = 1,
                day_of_week = "Tuesday",
                have_break_time = false,
                start_break_time = null,
                stop_break_time = null,
                opening_time = "09:00",
                closing_time = "22:00",
                is_holiday = false
            )
        )

        val exampleRestaurant = Restaurant(
            id = 1,
            name = "Test Restaurant",
            door_type = "정문",
            longitude = 127.0340,
            latitude = 37.5665,
            address = "서울시 종로구 대학로",
            phone_number = "010-1234-5678",
            rating = 4.5,
            category = "한식",
            sub_description = "음식점",
            description = "맛있는 한식 전문점",
            review_count = 10,
            bookmark_count = 5,
            businessDays = exampleBusinessDays
        )
        _restaurants.value = listOf(exampleRestaurant)
    }
}
