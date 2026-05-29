package com.rewardly.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "tasks")
data class Task(
    @Id @GeneratedValue val id: UUID = UUID.randomUUID(),
    val title: String,
    val description: String,
    @Enumerated(EnumType.STRING) val category: TaskCategory,
    val tokenValue: Int,
    val dueDateNote: String,
    @Enumerated(EnumType.STRING) var status: TaskStatus = TaskStatus.OPEN,
    @ManyToOne @JoinColumn(name = "assigned_to") val assignedTo: User,
    @ManyToOne @JoinColumn(name = "created_by") val createdBy: User,
    var startedAt: LocalDateTime? = null,
    var finishedAt: LocalDateTime? = null,
    var childNote: String? = null,
    var childSelfRating: Int? = null,
    var parentRating: Int? = null,
    var parentComment: String? = null,
    var tokensEarned: Int? = null
)

enum class TaskStatus { OPEN, IN_PROGRESS, DONE, REVIEWED }

enum class TaskCategory {
    SPORT_AND_ACTIVITY,
    STUDY_AND_LEARNING,
    CHORES,
    HEALTH_AND_HYGIENE,
    CREATIVE,
    SOCIAL_AND_FAMILY,
    DIGITAL_WELLBEING
}
