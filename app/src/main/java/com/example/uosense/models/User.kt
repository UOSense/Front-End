package com.example.uosense.models

data class NewUserRequest(
    val email: String,
    val password: String,
    val nickname: String
)

data class WebmailRequest(
    val email: String,
    val purpose: String // 인증 목적 예: "SIGNUP"
)

data class AuthCodeRequest(
    val email: String,
    val code: String // 사용자가 입력한 인증 코드
)