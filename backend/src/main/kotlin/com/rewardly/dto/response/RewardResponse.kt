package com.rewardly.dto.response

import java.util.UUID

data class RewardResponse(
    val id: UUID,
    val title: String,
    val description: String,
    val tokenCost: Int,
    val assignToAll: Boolean,
    val assignedTo: UserSummaryResponse?
)
