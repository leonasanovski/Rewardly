package com.rewardly.dto.request

data class ReviewTaskRequest(
    val parentRating: Int,
    val parentComment: String? = null
)
