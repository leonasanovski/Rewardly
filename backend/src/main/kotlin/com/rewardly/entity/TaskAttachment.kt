package com.rewardly.entity

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "task_attachments")
data class TaskAttachment(
    @Id @GeneratedValue val id: UUID = UUID.randomUUID(),
    @ManyToOne @JoinColumn(name = "task_id") val task: Task,
    val fileName: String,
    val filePath: String,
    val fileType: String
)
