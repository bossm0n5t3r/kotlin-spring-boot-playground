package me.bossm0n5t3r.account.util

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.UnsupportedJwtException
import io.jsonwebtoken.security.SignatureException
import me.bossm0n5t3r.account.security.TokenValidationFailedException
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
        try {
            Jwts
                .parser()
                .verifyWith(ecdsaKeyPair.public)
                .build()
                .parseSignedClaims(token)
                .payload
                .subject
        } catch (e: Exception) {
            throw handleException(e)
        }

    fun validateToken(token: String): Boolean =
        try {
            Jwts
                .parser()
                .verifyWith(ecdsaKeyPair.public)
                .build()
                .parseSignedClaims(token)
            true
        } catch (e: Exception) {
            throw handleException(e)
        }

    private fun handleException(e: Exception): TokenValidationFailedException =
        when (e) {
            is SignatureException -> TokenValidationFailedException("Invalid JWT signature", e)
            is MalformedJwtException -> TokenValidationFailedException("Invalid JWT token", e)
            is ExpiredJwtException -> TokenValidationFailedException("Expired JWT token", e)
            is UnsupportedJwtException -> TokenValidationFailedException("Unsupported JWT token", e)
            is IllegalArgumentException -> TokenValidationFailedException("JWT claims string is empty", e)
            else -> TokenValidationFailedException("JWT validation failed", e)
        }
}
