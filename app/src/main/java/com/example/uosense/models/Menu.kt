package com.example.uosense.models

data class MenuResponse(
    val id: Int,
    val restaurantId: Int,
    val name: String,
    val price: Int,
    val description: String,
    val imageUrl: String
)

data class MenuRequest(
    val id: Int,
    val restaurantId: Int,
    val name: String,
    val price: Int,
    val description: String
)
