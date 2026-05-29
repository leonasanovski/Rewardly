package com.rewardly.repository

import com.rewardly.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserRepository : JpaRepository<User, UUID> {
    fun findByEmail(email: String): User?
    fun findByUsername(username: String): User?
    fun findAllByParentId(parentId: UUID): List<User>
}
