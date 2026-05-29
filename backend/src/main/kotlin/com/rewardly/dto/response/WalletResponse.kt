package com.rewardly.dto.response

data class WalletResponse(
    val tokens: Int,
    val purchasedRewards: List<PurchasedRewardResponse>
)
