package com.rewardly.repository

import com.rewardly.entity.PurchasedReward
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PurchasedRewardRepository : JpaRepository<PurchasedReward, UUID> {
    fun findAllByChildId(childId: UUID): List<PurchasedReward>
    fun findAllByRewardId(rewardId: UUID): List<PurchasedReward>
}
