package me.bossm0n5t3r.account.util

import io.jsonwebtoken.Jwts
import org.springframework.stereotype.Component
import java.security.KeyPair
import java.util.Date

@Component
class JwtProvider(
    private val ecdsaKeyPair: KeyPair,
    private val expiration: Long,
) {
    fun createToken(username: String): String {
        val now = Date()
        val expiryDate = Date(now.time + expiration * 1000)

        return Jwts
            .builder()
            .subject(username)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(ecdsaKeyPair.private)
            .compact()
    }

    fun getUsernameFromToken(token: String): String =
        Jwts
            .parser()
            .verifyWith(ecdsaKeyPair.public)
            .build()
            .parseSignedClaims(token)
            .payload
            .subject

    fun validateToken(token: String): Boolean =
        try {
            Jwts
                .parser()
                .verifyWith(ecdsaKeyPair.public)
                .build()
                .parseSignedClaims(token)
            true
        } catch (e: Exception) {
            false
        }
}
