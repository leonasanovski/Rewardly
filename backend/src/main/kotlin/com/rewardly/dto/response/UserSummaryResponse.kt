package com.rewardly.dto.response

import java.util.UUID

data class UserSummaryResponse(
    val id: UUID,
    val fullName: String,
    val username: String?
)
