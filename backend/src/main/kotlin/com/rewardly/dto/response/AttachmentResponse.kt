package com.rewardly.dto.response

import java.util.UUID

data class AttachmentResponse(
    val id: UUID,
    val fileName: String,
    val filePath: String,
    val fileType: String
)
