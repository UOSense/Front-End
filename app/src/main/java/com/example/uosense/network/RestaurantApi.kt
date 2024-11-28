package com.example.uosense.network

import com.example.uosense.models.*
import okhttp3.MultipartBody
import retrofit2.http.*

interface RestaurantApi {
    @GET("/show")
    suspend fun getAllRestaurants(
        @Query("doorType") doorType: String?,
        @Query("category") category: String?
    ): List<RestaurantListResponse>

    @GET("/{restaurantId}/show")
    suspend fun getRestaurantById(
        @Path("restaurantId") restaurantId: Int
    ): RestaurantInfo

    @POST("/restaurant/new")
    suspend fun createRestaurant(
        @Body restaurantRequest: RestaurantRequest
    )

    @PUT("/restaurant/{restaurantId}/update")
    suspend fun editRestaurant(
        @Path("restaurantId") restaurantId: Int,
        @Body restaurantRequest: RestaurantRequest
    )

    @DELETE("/restaurant/{restaurantId}/delete")
    suspend fun deleteRestaurant(
        @Path("restaurantId") restaurantId: Int
    )

    @GET("/{restaurantId}/menu")
    suspend fun getMenuByRestaurantId(
        @Path("restaurantId") restaurantId: Int
    ): List<MenuResponse>
}
