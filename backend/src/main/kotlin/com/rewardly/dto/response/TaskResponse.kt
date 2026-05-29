package com.rewardly.dto.response

import com.rewardly.entity.TaskCategory
import com.rewardly.entity.TaskStatus
import java.time.LocalDateTime
import java.util.UUID

data class TaskResponse(
    val id: UUID,
    val title: String,
    val description: String,
    val category: TaskCategory,
    val tokenValue: Int,
    val dueDateNote: String,
    val status: TaskStatus,
    val assignedTo: UserSummaryResponse,
    val startedAt: LocalDateTime?,
    val finishedAt: LocalDateTime?,
    val childNote: String?,
    val childSelfRating: Int?,
    val parentRating: Int?,
    val parentComment: String?,
    val tokensEarned: Int?
)
