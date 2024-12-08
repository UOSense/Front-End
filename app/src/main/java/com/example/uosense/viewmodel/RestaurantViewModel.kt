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
