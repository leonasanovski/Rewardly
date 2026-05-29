package com.rewardly.dto.response

import com.rewardly.entity.RewardStatus
import java.time.LocalDateTime
import java.util.UUID

data class PurchasedRewardResponse(
    val id: UUID,
    val reward: RewardSummaryResponse,
    val status: RewardStatus,
    val purchasedAt: LocalDateTime,
    val redeemedAt: LocalDateTime?
)
