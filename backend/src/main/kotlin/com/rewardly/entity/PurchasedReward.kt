package com.rewardly.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "purchased_rewards")
data class PurchasedReward(
    @Id @GeneratedValue val id: UUID = UUID.randomUUID(),
    @ManyToOne @JoinColumn(name = "reward_id") val reward: Reward,
    @ManyToOne @JoinColumn(name = "child_id") val child: User,
    @Enumerated(EnumType.STRING) var status: RewardStatus = RewardStatus.AVAILABLE,
    val purchasedAt: LocalDateTime = LocalDateTime.now(),
    var redeemedAt: LocalDateTime? = null
)

enum class RewardStatus { AVAILABLE, REDEEMED }
