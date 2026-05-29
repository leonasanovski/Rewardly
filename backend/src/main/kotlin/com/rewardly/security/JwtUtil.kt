package com.rewardly.security

import com.rewardly.entity.User
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.springframework.stereotype.Component
import java.util.Date
import java.util.UUID

@Component
class JwtUtil {
    private val secret = "rewardly-secret-key-change-in-production"
    private val expiration = 86400000L

    fun generateToken(user: User): String {
        return Jwts.builder()
            .setSubject(user.id.toString())
            .claim("role", user.role.name)
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + expiration))
            .signWith(SignatureAlgorithm.HS256, secret)
            .compact()
    }

    fun extractUserId(token: String): UUID =
        UUID.fromString(Jwts.parser().setSigningKey(secret).parseClaimsJws(token).body.subject)

    fun extractRole(token: String): String =
        Jwts.parser().setSigningKey(secret).parseClaimsJws(token).body["role"] as String

    fun isTokenValid(token: String): Boolean = try {
        Jwts.parser().setSigningKey(secret).parseClaimsJws(token); true
    } catch (e: Exception) { false }
}
