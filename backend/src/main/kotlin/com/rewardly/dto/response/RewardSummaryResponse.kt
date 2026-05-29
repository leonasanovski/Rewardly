package com.rewardly.dto.response

import java.util.UUID

data class RewardSummaryResponse(
    val id: UUID,
    val title: String,
    val description: String,
    val tokenCost: Int
)
