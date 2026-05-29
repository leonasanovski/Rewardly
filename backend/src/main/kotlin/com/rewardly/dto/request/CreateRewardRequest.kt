package com.rewardly.dto.request

import java.util.UUID

data class CreateRewardRequest(
    val title: String,
    val description: String,
    val tokenCost: Int,
    val assignToAll: Boolean,
    val assignedToId: UUID? = null
)
