package com.rewardly.controller

import com.rewardly.dto.request.AdjustTokensRequest
import com.rewardly.dto.request.CreateChildRequest
import com.rewardly.dto.request.UpdateChildRequest
import com.rewardly.dto.response.ChildResponse
import com.rewardly.entity.Role
import com.rewardly.entity.User
import com.rewardly.repository.PurchasedRewardRepository
import com.rewardly.repository.TaskRepository
import com.rewardly.repository.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/children")
@PreAuthorize("hasRole('PARENT')")
class ChildrenController(
    val userRepository: UserRepository,
    val taskRepository: TaskRepository,
    val purchasedRewardRepository: PurchasedRewardRepository,
    val passwordEncoder: PasswordEncoder
) {

    @GetMapping
    fun getChildren(@AuthenticationPrincipal parent: User): List<ChildResponse> =
        userRepository.findAllByParentId(parent.id).map { it.toResponse() }

    @PostMapping
    fun createChild(
        @AuthenticationPrincipal parent: User,
        @RequestBody req: CreateChildRequest
    ): ResponseEntity<Any> {
        if (userRepository.findByUsername(req.username) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(mapOf("error" to "Username already taken"))
        }
        val child = userRepository.save(
            User(
                fullName = req.fullName,
                username = req.username,
                passwordHash = passwordEncoder.encode(req.password),
                role = Role.CHILD,
                parent = parent
            )
        )
        return ResponseEntity.ok(child.toResponse())
    }

    @PutMapping("/{id}")
    fun updateChild(
        @AuthenticationPrincipal parent: User,
        @PathVariable id: UUID,
        @RequestBody req: UpdateChildRequest
    ): ResponseEntity<Any> {
        val child = resolveOwnedChild(id, parent)
            ?: return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("error" to "Child not found or not owned by you"))

        if (req.username != null && req.username != child.username &&
            userRepository.findByUsername(req.username) != null
        ) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(mapOf("error" to "Username already taken"))
        }

        val updated = userRepository.save(
            child.copy(
                fullName = req.fullName ?: child.fullName,
                username = req.username ?: child.username,
                passwordHash = if (req.password != null) passwordEncoder.encode(req.password)
                               else child.passwordHash
            )
        )
        return ResponseEntity.ok(updated.toResponse())
    }

    @DeleteMapping("/{id}")
    fun deleteChild(
        @AuthenticationPrincipal parent: User,
        @PathVariable id: UUID
    ): ResponseEntity<Any> {
        val child = resolveOwnedChild(id, parent)
            ?: return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("error" to "Child not found or not owned by you"))

        taskRepository.deleteAll(taskRepository.findAllByAssignedToId(child.id))
        purchasedRewardRepository.deleteAll(purchasedRewardRepository.findAllByChildId(child.id))
        userRepository.delete(child)

        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/{id}/tokens")
    fun adjustTokens(
        @AuthenticationPrincipal parent: User,
        @PathVariable id: UUID,
        @RequestBody req: AdjustTokensRequest
    ): ResponseEntity<Any> {
        val child = resolveOwnedChild(id, parent)
            ?: return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("error" to "Child not found or not owned by you"))

        child.tokens = maxOf(0, child.tokens + req.amount)
        return ResponseEntity.ok(userRepository.save(child).toResponse())
    }

    @GetMapping("/{id}/credentials")
    fun getCredentials(
        @AuthenticationPrincipal parent: User,
        @PathVariable id: UUID
    ): ResponseEntity<Any> {
        val child = resolveOwnedChild(id, parent)
            ?: return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("error" to "Child not found or not owned by you"))

        return ResponseEntity.ok(
            mapOf("credentials" to "Username: ${child.username}\nPassword: ••••••••")
        )
    }

    private fun resolveOwnedChild(childId: UUID, parent: User): User? {
        val child = userRepository.findById(childId).orElse(null) ?: return null
        return if (child.role == Role.CHILD && child.parent?.id == parent.id) child else null
    }

    private fun User.toResponse() = ChildResponse(
        id = id,
        fullName = fullName,
        username = username ?: "",
        tokens = tokens
    )
}
