package com.rewardly.dto.request

import com.rewardly.entity.TaskCategory
import java.util.UUID

data class CreateTaskRequest(
    val title: String,
    val description: String,
    val category: TaskCategory,
    val tokenValue: Int,
    val dueDateNote: String,
    val assignToAll: Boolean,
    val assignedToId: UUID? = null
)
