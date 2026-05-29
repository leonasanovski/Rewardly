package com.rewardly.dto.request

data class RegisterRequest(
    val fullName: String,
    val email: String,
    val password: String,
    val embg: String
)
