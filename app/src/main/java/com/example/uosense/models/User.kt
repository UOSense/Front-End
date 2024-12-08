package com.example.uosense.models

data class NewUserRequest(
    val email: String,
    val password: String,
    val nickname: String
)