package com.rewardly.dto.request

data class UpdateRewardRequest(
    val title: String? = null,
    val description: String? = null,
    val tokenCost: Int? = null
)
