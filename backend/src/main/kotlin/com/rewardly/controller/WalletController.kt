package com.rewardly.controller

import com.rewardly.dto.response.PurchasedRewardResponse
import com.rewardly.dto.response.RewardSummaryResponse
import com.rewardly.dto.response.WalletResponse
import com.rewardly.entity.*
import com.rewardly.repository.PurchasedRewardRepository
import com.rewardly.repository.RewardRepository
import com.rewardly.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import java.util.UUID

@RestController
class WalletController(
    val userRepository: UserRepository,
    val rewardRepository: RewardRepository,
    val purchasedRewardRepository: PurchasedRewardRepository
) {

    @GetMapping("/api/wallet")
    @PreAuthorize("hasRole('CHILD')")
    fun getMyWallet(@AuthenticationPrincipal child: User): WalletResponse =
        buildWallet(child)

    @GetMapping("/api/wallet/{childId}")
    @PreAuthorize("hasRole('PARENT')")
    fun getChildWallet(
        @AuthenticationPrincipal parent: User,
        @PathVariable childId: UUID
    ): ResponseEntity<Any> {
        val child = userRepository.findById(childId).orElse(null)
        if (child == null || child.role != Role.CHILD || child.parent?.id != parent.id) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("error" to "Child not found or not owned by you"))
        }
        return ResponseEntity.ok(buildWallet(child))
    }

    @PutMapping("/api/purchased/{id}/redeem")
    @PreAuthorize("hasRole('PARENT')")
    fun redeemReward(
        @AuthenticationPrincipal parent: User,
        @PathVariable id: UUID
    ): ResponseEntity<Any> {
        val purchased = purchasedRewardRepository.findById(id).orElse(null)
            ?: return ResponseEntity.notFound().build()

        if (purchased.child.parent?.id != parent.id) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("error" to "This reward does not belong to one of your children"))
        }
        if (purchased.status != RewardStatus.AVAILABLE) {
            return ResponseEntity.badRequest()
                .body(mapOf("error" to "Reward is already redeemed"))
        }

        purchased.status = RewardStatus.REDEEMED
        purchased.redeemedAt = LocalDateTime.now()
        return ResponseEntity.ok(purchasedRewardRepository.save(purchased).toResponse())
    }

    @PostMapping("/api/rewards/{id}/buy")
    @PreAuthorize("hasRole('CHILD')")
    fun buyReward(
        @AuthenticationPrincipal child: User,
        @PathVariable id: UUID
    ): ResponseEntity<Any> {
        val reward = rewardRepository.findById(id).orElse(null)
            ?: return ResponseEntity.notFound().build()

        val parentId = child.parent?.id
        if (parentId == null || reward.createdBy.id != parentId ||
            (!reward.assignToAll && reward.assignedTo?.id != child.id)
        ) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("error" to "Reward is not available to you"))
        }

        if (child.tokens < reward.tokenCost) {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(
                mapOf(
                    "error" to "Not enough tokens",
                    "required" to reward.tokenCost,
                    "available" to child.tokens
                )
            )
        }

        child.tokens -= reward.tokenCost
        userRepository.save(child)

        val purchased = purchasedRewardRepository.save(
            PurchasedReward(reward = reward, child = child)
        )
        return ResponseEntity.ok(purchased.toResponse())
    }

    private fun buildWallet(child: User): WalletResponse {
        val purchases = purchasedRewardRepository.findAllByChildId(child.id)
        return WalletResponse(
            tokens = child.tokens,
            purchasedRewards = purchases.map { it.toResponse() }
        )
    }

    private fun PurchasedReward.toResponse() = PurchasedRewardResponse(
        id = id,
        reward = RewardSummaryResponse(
            id = reward.id,
            title = reward.title,
            description = reward.description,
            tokenCost = reward.tokenCost
        ),
        status = status,
        purchasedAt = purchasedAt,
        redeemedAt = redeemedAt
    )
}
