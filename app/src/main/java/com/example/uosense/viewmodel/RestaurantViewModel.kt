package com.example.uosense.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uosense.models.RestaurantInfo
import com.example.uosense.models.RestaurantListResponse
import com.example.uosense.network.RetrofitInstance
import kotlinx.coroutines.launch

class RestaurantViewModel : ViewModel() {
    private val api = RetrofitInstance.restaurantApi

    val restaurantList = MutableLiveData<List<RestaurantListResponse>>()
    val restaurantInfo = MutableLiveData<RestaurantInfo?>()
    val isBookmarked = MutableLiveData<Boolean>()

    fun fetchAllRestaurants(doorType: String? = null, filter: String? = "DEFAULT") {
        viewModelScope.launch {
            try {
                val response = api.getRestaurantList(doorType, filter)
                restaurantList.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun fetchRestaurantById(restaurantId: Int) {
        viewModelScope.launch {
            try {
                val response = api.getRestaurantById(restaurantId)
                restaurantInfo.postValue(response)
            } catch (e: Exception) {
                restaurantInfo.postValue(null)
                e.printStackTrace()
            }
        }
    }

    fun addBookmark(restaurantId: Int) {
        viewModelScope.launch {
            try {
                val response = api.addBookmark(restaurantId)
                isBookmarked.postValue(response.isSuccessful)
            } catch (e: Exception) {
                e.printStackTrace()
                isBookmarked.postValue(false)
            }
        }
    }

    fun deleteBookmark(bookmarkId: Int) {
        viewModelScope.launch {
            try {
                val response = api.deleteBookmark(bookmarkId)
                isBookmarked.postValue(!response.isSuccessful)
            } catch (e: Exception) {
                e.printStackTrace()
                isBookmarked.postValue(true)
            }
        }
    }
}
