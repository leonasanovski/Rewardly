package com.rewardly.dto.response

import java.util.UUID

data class ChildResponse(
    val id: UUID,
    val fullName: String,
    val username: String,
    val tokens: Int
)
