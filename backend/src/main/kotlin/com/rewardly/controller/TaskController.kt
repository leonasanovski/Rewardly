package com.rewardly.controller

import com.rewardly.dto.request.CreateTaskRequest
import com.rewardly.dto.request.ReviewTaskRequest
import com.rewardly.dto.request.SubmitTaskRequest
import com.rewardly.dto.response.AttachmentResponse
import com.rewardly.dto.response.TaskResponse
import com.rewardly.dto.response.UserSummaryResponse
import com.rewardly.entity.*
import com.rewardly.repository.TaskAttachmentRepository
import com.rewardly.repository.TaskRepository
import com.rewardly.repository.UserRepository
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.time.LocalDateTime
import java.util.UUID
import kotlin.math.roundToInt

@RestController
@RequestMapping("/api/tasks")
class TaskController(
    val taskRepository: TaskRepository,
    val userRepository: UserRepository,
    val taskAttachmentRepository: TaskAttachmentRepository
) {

    @GetMapping
    fun getTasks(
        @AuthenticationPrincipal user: User,
        @RequestParam childId: UUID? = null,
        @RequestParam status: TaskStatus? = null
    ): List<TaskResponse> {
        return when (user.role) {
            Role.PARENT -> {
                var tasks = taskRepository.findAllByCreatedById(user.id)
                if (childId != null) tasks = tasks.filter { it.assignedTo.id == childId }
                if (status != null) tasks = tasks.filter { it.status == status }
                tasks.map { it.toResponse() }
            }
            Role.CHILD -> taskRepository.findAllByAssignedToId(user.id).map { it.toResponse() }
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('PARENT')")
    fun createTask(
        @AuthenticationPrincipal parent: User,
        @RequestBody req: CreateTaskRequest
    ): ResponseEntity<Any> {
        val children: List<User> = if (req.assignToAll) {
            userRepository.findAllByParentId(parent.id)
        } else {
            val child = req.assignedToId
                ?.let { userRepository.findById(it).orElse(null) }
                ?: return ResponseEntity.badRequest()
                    .body(mapOf("error" to "assignedToId is required when assignToAll is false"))
            if (child.parent?.id != parent.id) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(mapOf("error" to "Child does not belong to you"))
            }
            listOf(child)
        }

        val tasks = children.map { child ->
            taskRepository.save(
                Task(
                    title = req.title,
                    description = req.description,
                    category = req.category,
                    tokenValue = req.tokenValue,
                    dueDateNote = req.dueDateNote,
                    assignedTo = child,
                    createdBy = parent
                )
            )
        }
        return ResponseEntity.ok(tasks.map { it.toResponse() })
    }

    @GetMapping("/{id}")
    fun getTask(
        @AuthenticationPrincipal user: User,
        @PathVariable id: UUID
    ): ResponseEntity<Any> {
        val task = taskRepository.findById(id).orElse(null)
            ?: return ResponseEntity.notFound().build()
        if (!canView(user, task)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(mapOf("error" to "Access denied"))
        }
        return ResponseEntity.ok(task.toResponse())
    }

    @PutMapping("/{id}/start")
    @PreAuthorize("hasRole('CHILD')")
    fun startTask(
        @AuthenticationPrincipal child: User,
        @PathVariable id: UUID
    ): ResponseEntity<Any> {
        val task = resolveAssignedTask(id, child)
            ?: return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("error" to "Task not found or not assigned to you"))
        if (task.status != TaskStatus.OPEN) {
            return ResponseEntity.badRequest().body(mapOf("error" to "Task must be OPEN to start"))
        }
        task.status = TaskStatus.IN_PROGRESS
        task.startedAt = LocalDateTime.now()
        return ResponseEntity.ok(taskRepository.save(task).toResponse())
    }

    @PutMapping("/{id}/submit")
    @PreAuthorize("hasRole('CHILD')")
    fun submitTask(
        @AuthenticationPrincipal child: User,
        @PathVariable id: UUID,
        @RequestBody req: SubmitTaskRequest
    ): ResponseEntity<Any> {
        val task = resolveAssignedTask(id, child)
            ?: return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("error" to "Task not found or not assigned to you"))
        if (task.status != TaskStatus.IN_PROGRESS) {
            return ResponseEntity.badRequest().body(mapOf("error" to "Task must be IN_PROGRESS to submit"))
        }
        task.status = TaskStatus.DONE
        task.finishedAt = LocalDateTime.now()
        task.childNote = req.childNote
        task.childSelfRating = req.childSelfRating
        return ResponseEntity.ok(taskRepository.save(task).toResponse())
    }

    @PutMapping("/{id}/review")
    @PreAuthorize("hasRole('PARENT')")
    fun reviewTask(
        @AuthenticationPrincipal parent: User,
        @PathVariable id: UUID,
        @RequestBody req: ReviewTaskRequest
    ): ResponseEntity<Any> {
        val task = taskRepository.findById(id).orElse(null)
            ?: return ResponseEntity.notFound().build()
        if (task.createdBy.id != parent.id) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("error" to "Task not created by you"))
        }
        if (task.status != TaskStatus.DONE) {
            return ResponseEntity.badRequest().body(mapOf("error" to "Task must be DONE to review"))
        }

        val earned = calculateTokens(req.parentRating, task.tokenValue)
        task.status = TaskStatus.REVIEWED
        task.parentRating = req.parentRating
        task.parentComment = req.parentComment
        task.tokensEarned = earned

        val child = task.assignedTo
        child.tokens += earned
        userRepository.save(child)

        return ResponseEntity.ok(taskRepository.save(task).toResponse())
    }

    @PostMapping("/{id}/attachments", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @PreAuthorize("hasRole('CHILD')")
    fun uploadAttachments(
        @AuthenticationPrincipal child: User,
        @PathVariable id: UUID,
        @RequestParam("files") files: List<MultipartFile>
    ): ResponseEntity<Any> {
        val task = resolveAssignedTask(id, child)
            ?: return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("error" to "Task not found or not assigned to you"))

        val uploadDir = File("uploads/${task.id}").absoluteFile
        uploadDir.mkdirs()

        val attachments = files.map { file ->
            val fileName = file.originalFilename ?: "file"
            val dest = File(uploadDir, fileName)
            file.transferTo(dest)
            taskAttachmentRepository.save(
                TaskAttachment(
                    task = task,
                    fileName = fileName,
                    filePath = dest.path,
                    fileType = file.contentType ?: "application/octet-stream"
                )
            )
        }
        return ResponseEntity.ok(attachments.map { it.toResponse() })
    }

    @GetMapping("/{taskId}/attachments/{attachmentId}/download")
    fun downloadAttachment(
        @AuthenticationPrincipal user: User,
        @PathVariable taskId: UUID,
        @PathVariable attachmentId: UUID
    ): ResponseEntity<Resource> {
        val task = taskRepository.findById(taskId).orElse(null)
            ?: return ResponseEntity.notFound().build()
        if (!canView(user, task)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        val attachment = taskAttachmentRepository.findById(attachmentId).orElse(null)
            ?: return ResponseEntity.notFound().build()
        if (attachment.task.id != taskId) {
            return ResponseEntity.badRequest().build()
        }
        val file = File(attachment.filePath)
        if (!file.exists()) {
            return ResponseEntity.notFound().build()
        }
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${attachment.fileName}\"")
            .contentType(MediaType.parseMediaType(attachment.fileType))
            .body(FileSystemResource(file))
    }

    @GetMapping("/{id}/attachments")
    fun getAttachments(
        @AuthenticationPrincipal user: User,
        @PathVariable id: UUID
    ): ResponseEntity<Any> {
        val task = taskRepository.findById(id).orElse(null)
            ?: return ResponseEntity.notFound().build()
        if (!canView(user, task)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(mapOf("error" to "Access denied"))
        }
        return ResponseEntity.ok(taskAttachmentRepository.findAllByTaskId(id).map { it.toResponse() })
    }

    private fun canView(user: User, task: Task): Boolean = when (user.role) {
        Role.PARENT -> task.createdBy.id == user.id
        Role.CHILD -> task.assignedTo.id == user.id
    }

    private fun resolveAssignedTask(taskId: UUID, child: User): Task? {
        val task = taskRepository.findById(taskId).orElse(null) ?: return null
        return if (task.assignedTo.id == child.id) task else null
    }

    private fun calculateTokens(rating: Int, tokenValue: Int): Int =
        ((rating / 5.0) * tokenValue).roundToInt()

    private fun Task.toResponse() = TaskResponse(
        id = id,
        title = title,
        description = description,
        category = category,
        tokenValue = tokenValue,
        dueDateNote = dueDateNote,
        status = status,
        assignedTo = UserSummaryResponse(
            id = assignedTo.id,
            fullName = assignedTo.fullName,
            username = assignedTo.username
        ),
        startedAt = startedAt,
        finishedAt = finishedAt,
        childNote = childNote,
        childSelfRating = childSelfRating,
        parentRating = parentRating,
        parentComment = parentComment,
        tokensEarned = tokensEarned
    )

    private fun TaskAttachment.toResponse() = AttachmentResponse(
        id = id,
        fileName = fileName,
        filePath = filePath,
        fileType = fileType
    )
}
