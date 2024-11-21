package com.example.uosense.network

import com.example.uosense.models.Restaurant
import retrofit2.Call
import retrofit2.http.*

interface RestaurantApi {

    @GET("/api/restaurants")
    fun getRestaurants(): Call<List<Restaurant>> // 식당 목록 가져오기

    @POST("/api/restaurants")
    fun addRestaurant(@Body restaurant: Restaurant): Call<Void> // 식당 추가

    @PUT("/api/restaurants/{id}")
    fun updateRestaurant(@Path("id") id: Int, @Body restaurant: Restaurant): Call<Void> // 식당 수정

    @DELETE("/api/restaurants/{id}")
    fun deleteRestaurant(@Path("id") id: Int): Call<Void> // 식당 삭제
}