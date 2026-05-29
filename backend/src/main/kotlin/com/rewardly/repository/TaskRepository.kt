package com.rewardly.repository

import com.rewardly.entity.Task
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface TaskRepository : JpaRepository<Task, UUID> {
    fun findAllByAssignedToId(assignedToId: UUID): List<Task>
    fun findAllByCreatedById(createdById: UUID): List<Task>
}
