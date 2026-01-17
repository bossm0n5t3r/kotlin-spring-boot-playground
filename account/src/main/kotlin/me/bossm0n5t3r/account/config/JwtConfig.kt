package me.bossm0n5t3r.account.config

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import java.security.KeyPair
import java.security.SecureRandom
import javax.crypto.SecretKey
import kotlin.io.encoding.Base64

@Configuration
class JwtConfig {
    @Bean
    @Primary
    fun secret(): String {
        val key = ByteArray(64)
        SecureRandom().nextBytes(key)
        return Base64.encode(key)
    }

    @Bean
    @Primary
    fun expiration(): Long = 3600L

    @Bean
    fun secretKey(
        @Qualifier("secret") secret: String,
    ): SecretKey = Keys.hmacShaKeyFor(secret.toByteArray())

    @Bean
    fun ecdsaKeyPair(): KeyPair =
        Jwts.SIG.ES256
            .keyPair()
            .build()
}
