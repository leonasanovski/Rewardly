package com.rewardly.repository

import com.rewardly.entity.TaskAttachment
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface TaskAttachmentRepository : JpaRepository<TaskAttachment, UUID> {
    fun findAllByTaskId(taskId: UUID): List<TaskAttachment>
}
