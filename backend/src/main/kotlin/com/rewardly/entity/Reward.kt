package com.rewardly.entity

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "rewards")
data class Reward(
    @Id @GeneratedValue val id: UUID = UUID.randomUUID(),
    val title: String,
    val description: String,
    val tokenCost: Int,
    val assignToAll: Boolean = false,
    @ManyToOne @JoinColumn(name = "assigned_to") val assignedTo: User? = null,
    @ManyToOne @JoinColumn(name = "created_by") val createdBy: User
)
