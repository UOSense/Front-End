package com.example.uosense.network

import com.example.uosense.models.Restaurant
import retrofit2.Call
import retrofit2.http.*

interface RestaurantApi {

    // 모든 식당 정보 조회 (Coroutine 방식)
    @GET("/api/restaurants")
    suspend fun getAllRestaurants(): List<Restaurant>

    // 모든 식당 정보 조회 (Call 방식 - AdminActivity 호환)
    @GET("/api/restaurants")
    fun getRestaurants(): Call<List<Restaurant>>

    // 특정 식당 정보 조회
    @GET("/api/restaurants/{restaurantId}")
    suspend fun getRestaurant(@Path("restaurantId") id: Int): Restaurant

    // 문(door) 타입과 카테고리로 식당 필터링 조회
    @GET("/api/restaurants/filter")
    suspend fun getRestaurantsByDoorTypeAndCategory(
        @Query("doorType") doorType: String,
        @Query("category") category: String
    ): List<Restaurant>

    // 새로운 식당 추가
    @POST("/api/restaurants")
    fun addRestaurant(@Body restaurant: Restaurant): Call<Void>

    // 기존 식당 수정
    @PUT("/api/restaurants/{restaurantId}")
    fun updateRestaurant(
        @Path("restaurantId") restaurantId: Long,
        @Body restaurant: Restaurant
    ): Call<Void>

    // 특정 식당 삭제
    @DELETE("/api/restaurants/{restaurantId}")
    fun deleteRestaurant(@Path("restaurantId") restaurantId: Long): Call<Void>
}
