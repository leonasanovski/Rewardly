package com.rewardly.dto.response

data class AuthResponse(
    val token: String,
    val user: UserInfo
) {
    data class UserInfo(
        val id: String,
        val fullName: String,
        val username: String?,
        val email: String?,
        val role: String,
        val tokens: Int
    )
}
