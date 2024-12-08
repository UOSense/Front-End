package com.example.uosense.network

import RestaurantApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private var BASE_URL = "http://10.0.2.2:8080"
    /*
    유동적으로 서버가 계속 바뀌므로
    서버 사용 시
    /res/xml/network_security_config 수정 필요
     */


    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }


    val restaurantApi: RestaurantApi by lazy {
        retrofit.create(RestaurantApi::class.java)

    }
}

