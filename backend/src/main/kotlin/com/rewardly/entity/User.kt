package com.rewardly.entity

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "users")
data class User(
    @Id @GeneratedValue val id: UUID = UUID.randomUUID(),
    val fullName: String,
    val email: String? = null,
    val username: String? = null,
    val passwordHash: String,
    @Enumerated(EnumType.STRING) val role: Role,
    val embg: String? = null,
    var tokens: Int = 0,
    @ManyToOne @JoinColumn(name = "parent_id") val parent: User? = null
)

enum class Role { PARENT, CHILD }
