package com.rewardly.repository

import com.rewardly.entity.Reward
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface RewardRepository : JpaRepository<Reward, UUID> {
    fun findAllByCreatedById(createdById: UUID): List<Reward>
    fun findAllByAssignedToIdOrAssignToAll(assignedToId: UUID, assignToAll: Boolean): List<Reward>
}
