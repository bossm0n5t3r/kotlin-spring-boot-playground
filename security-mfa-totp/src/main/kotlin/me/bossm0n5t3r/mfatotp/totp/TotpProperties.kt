package me.bossm0n5t3r.mfatotp.totp

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("totp")
data class TotpProperties(
    val allowed: Allowed,
) {
    data class Allowed(
        val pastSteps: Int,
        val futureSteps: Int,
    )
}
