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

    // 식당 목록 가져오기
    fun fetchAllRestaurants(doorType: String? = null, filter: String? = "DEFAULT") {
        viewModelScope.launch {
            try {
                val response = api.getRestaurantList(doorType)
                if (response.isSuccessful && response.body() != null) {
                    restaurantList.postValue(response.body())
                } else {
                    restaurantList.postValue(emptyList())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                restaurantList.postValue(emptyList())
            }
        }
    }

    // 특정 식당 정보 가져오기
    fun fetchRestaurantById(restaurantId: Int) {
        viewModelScope.launch {
            try {
                val response = api.getRestaurantById(restaurantId)
                if (response.isSuccessful && response.body() != null) {
                    restaurantInfo.postValue(response.body())
                } else {
                    restaurantInfo.postValue(null)
                }
            } catch (e: Exception) {
                restaurantInfo.postValue(null)
                e.printStackTrace()
            }
        }
    }

    // 즐겨찾기 추가
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

    // 즐겨찾기 삭제
    fun deleteBookmark(bookmarkId: Int) {
        viewModelScope.launch {
            try {
                val response = api.deleteBookmark(bookmarkId)
                isBookmarked.postValue(response.isSuccessful.not())
            } catch (e: Exception) {
                e.printStackTrace()
                isBookmarked.postValue(true)
            }
        }
    }
}
