package me.bossm0n5t3r.mfatotp.configuration

import com.atlassian.onetime.core.TOTPGenerator
import com.atlassian.onetime.service.RandomSecretProvider
import com.atlassian.onetime.service.SecretProvider
import com.atlassian.onetime.service.TOTPConfiguration
import com.google.zxing.qrcode.QRCodeWriter
import java.time.Clock
import me.bossm0n5t3r.mfatotp.totp.TotpProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TotpBeanConfiguration(
    private val totpProperties: TotpProperties,
) {
    @Bean
    fun secretProvider(): SecretProvider {
        return RandomSecretProvider()
    }

    @Bean
    fun totpGenerator(clock: Clock): TOTPGenerator {
        return TOTPGenerator(
            clock = clock,
            startTime = totpProperties.startTime,
            timeStepSeconds = totpProperties.timeStepSeconds,
            otpLength = totpProperties.otpLength,
            digest = totpProperties.digest,
        )
    }

    @Bean
    fun totpConfiguration(): TOTPConfiguration {
        return TOTPConfiguration(
            allowedPastSteps = totpProperties.allowed.pastSteps,
            allowedFutureSteps = totpProperties.allowed.futureSteps,
        )
    }

    @Bean
    fun qrCodeWriter(): QRCodeWriter {
        return QRCodeWriter()
    }
}
