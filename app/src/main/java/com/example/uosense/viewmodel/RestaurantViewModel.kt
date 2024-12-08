package com.example.uosense.viewmodel


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.example.uosense.models.BusinessDay
import com.example.uosense.models.RestaurantInfo
import com.example.uosense.models.RestaurantListResponse
import com.example.uosense.network.RetrofitInstance
import kotlinx.coroutines.launch

class RestaurantViewModel : ViewModel() {
    private val api = RetrofitInstance.restaurantApi

    val restaurantList = MutableLiveData<List<RestaurantListResponse>>()
    val restaurantInfo = MutableLiveData<RestaurantInfo>()

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


    fun fetchAllRestaurants(doorType: String? = null, category: String? = null) {
        viewModelScope.launch {
            try {
                val response = api.getAllRestaurants(doorType, category)
                restaurantList.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    fun fetchRestaurantById(restaurantId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.restaurantApi.getRestaurantById(restaurantId)
                restaurantInfo.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }






}
