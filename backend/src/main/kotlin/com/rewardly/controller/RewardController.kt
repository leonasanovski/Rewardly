package com.rewardly.controller

import com.rewardly.dto.request.CreateRewardRequest
import com.rewardly.dto.request.UpdateRewardRequest
import com.rewardly.dto.response.RewardResponse
import com.rewardly.dto.response.UserSummaryResponse
import com.rewardly.entity.Reward
import com.rewardly.entity.Role
import com.rewardly.entity.User
import com.rewardly.repository.PurchasedRewardRepository
import com.rewardly.repository.RewardRepository
import com.rewardly.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/rewards")
class RewardController(
    val rewardRepository: RewardRepository,
    val userRepository: UserRepository,
    val purchasedRewardRepository: PurchasedRewardRepository
) {

    @GetMapping
    fun getRewards(@AuthenticationPrincipal user: User): List<RewardResponse> {
        return when (user.role) {
            Role.PARENT -> rewardRepository.findAllByCreatedById(user.id).map { it.toResponse() }
            Role.CHILD -> {
                val parentId = user.parent?.id ?: return emptyList()
                rewardRepository.findAllByCreatedById(parentId)
                    .filter { it.assignToAll || it.assignedTo?.id == user.id }
                    .map { it.toResponse() }
            }
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('PARENT')")
    fun createReward(
        @AuthenticationPrincipal parent: User,
        @RequestBody req: CreateRewardRequest
    ): ResponseEntity<Any> {
        val assignedTo: User? = if (!req.assignToAll) {
            val child = req.assignedToId
                ?.let { userRepository.findById(it).orElse(null) }
                ?: return ResponseEntity.badRequest()
                    .body(mapOf("error" to "assignedToId is required when assignToAll is false"))
            if (child.parent?.id != parent.id) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(mapOf("error" to "Child does not belong to you"))
            }
            child
        } else null

        val reward = rewardRepository.save(
            Reward(
                title = req.title,
                description = req.description,
                tokenCost = req.tokenCost,
                assignToAll = req.assignToAll,
                assignedTo = assignedTo,
                createdBy = parent
            )
        )
        return ResponseEntity.ok(reward.toResponse())
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PARENT')")
    fun updateReward(
        @AuthenticationPrincipal parent: User,
        @PathVariable id: UUID,
        @RequestBody req: UpdateRewardRequest
    ): ResponseEntity<Any> {
        val reward = resolveOwnedReward(id, parent)
            ?: return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("error" to "Reward not found or not owned by you"))

        val updated = rewardRepository.save(
            reward.copy(
                title = req.title ?: reward.title,
                description = req.description ?: reward.description,
                tokenCost = req.tokenCost ?: reward.tokenCost
            )
        )
        return ResponseEntity.ok(updated.toResponse())
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PARENT')")
    fun deleteReward(
        @AuthenticationPrincipal parent: User,
        @PathVariable id: UUID
    ): ResponseEntity<Any> {
        val reward = resolveOwnedReward(id, parent)
            ?: return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("error" to "Reward not found or not owned by you"))

        purchasedRewardRepository.deleteAll(purchasedRewardRepository.findAllByRewardId(reward.id))
        rewardRepository.delete(reward)
        return ResponseEntity.noContent().build()
    }

    private fun resolveOwnedReward(rewardId: UUID, parent: User): Reward? {
        val reward = rewardRepository.findById(rewardId).orElse(null) ?: return null
        return if (reward.createdBy.id == parent.id) reward else null
    }

    private fun Reward.toResponse() = RewardResponse(
        id = id,
        title = title,
        description = description,
        tokenCost = tokenCost,
        assignToAll = assignToAll,
        assignedTo = assignedTo?.let {
            UserSummaryResponse(id = it.id, fullName = it.fullName, username = it.username)
        }
    )
}
