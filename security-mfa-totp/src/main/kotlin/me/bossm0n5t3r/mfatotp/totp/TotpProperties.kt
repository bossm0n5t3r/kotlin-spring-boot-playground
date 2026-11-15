package me.bossm0n5t3r.mfatotp.totp

import com.atlassian.onetime.core.HMACDigest
import com.atlassian.onetime.core.OTPLength
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("totp")
data class TotpProperties(
    val allowed: Allowed,
    val startTime: Int,
    val timeStepSeconds: Int,
    val otpLength: OTPLength,
    val digest: HMACDigest,
) {
    data class Allowed(
        val pastSteps: Int,
        val futureSteps: Int,
    )
}
