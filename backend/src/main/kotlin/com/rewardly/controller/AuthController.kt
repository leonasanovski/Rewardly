package com.rewardly.controller

import com.rewardly.dto.request.LoginRequest
import com.rewardly.dto.request.RegisterRequest
import com.rewardly.dto.response.AuthResponse
import com.rewardly.entity.Role
import com.rewardly.entity.User
import com.rewardly.repository.UserRepository
import com.rewardly.security.JwtUtil
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@RestController
@RequestMapping("/api/auth")
class AuthController(
    val userRepository: UserRepository,
    val passwordEncoder: PasswordEncoder,
    val jwtUtil: JwtUtil
) {

    @PostMapping("/register")
    fun register(@RequestBody req: RegisterRequest): ResponseEntity<Any> {
        if (!isValidEmbg(req.embg)) {
            return ResponseEntity.badRequest().body(mapOf("error" to "Invalid EMBG format"))
        }
        if (!isAdult(req.embg)) {
            return ResponseEntity.badRequest().body(mapOf("error" to "User must be at least 18 years old"))
        }
        if (userRepository.findByEmail(req.email) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("error" to "Email already registered"))
        }
        val user = userRepository.save(
            User(
                fullName = req.fullName,
                email = req.email,
                passwordHash = passwordEncoder.encode(req.password),
                role = Role.PARENT,
                embg = req.embg
            )
        )
        return ResponseEntity.ok(user.toAuthResponse(jwtUtil))
    }

    @PostMapping("/login")
    fun login(@RequestBody req: LoginRequest): ResponseEntity<Any> {
        val user = userRepository.findByEmail(req.identifier)
            ?: userRepository.findByUsername(req.identifier)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "Invalid credentials"))

        if (!passwordEncoder.matches(req.password, user.passwordHash)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "Invalid credentials"))
        }
        return ResponseEntity.ok(user.toAuthResponse(jwtUtil))
    }

    private fun User.toAuthResponse(jwtUtil: JwtUtil) = AuthResponse(
        token = jwtUtil.generateToken(this),
        user = AuthResponse.UserInfo(
            id = id.toString(),
            fullName = fullName,
            username = username,
            email = email,
            role = role.name,
            tokens = tokens
        )
    )

    private fun isValidEmbg(embg: String): Boolean =
        embg.length == 13 && embg.all { it.isDigit() }

    private fun isAdult(embg: String): Boolean {
        if (!isValidEmbg(embg)) return false
        val day = embg.substring(0, 2).toInt()
        val month = embg.substring(2, 4).toInt()
        val yearSuffix = embg.substring(4, 7).toInt()
        val year = if (yearSuffix >= 800) 1000 + yearSuffix else 2000 + yearSuffix
        return try {
            val birthDate = LocalDate.of(year, month, day)
            ChronoUnit.YEARS.between(birthDate, LocalDate.now()) >= 18
        } catch (e: Exception) { false }
    }
}
