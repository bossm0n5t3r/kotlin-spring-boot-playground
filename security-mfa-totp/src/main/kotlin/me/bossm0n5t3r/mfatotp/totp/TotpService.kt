package me.bossm0n5t3r.mfatotp.totp

import com.atlassian.onetime.core.TOTP
import com.atlassian.onetime.core.TOTPGenerator
import com.atlassian.onetime.model.EmailAddress
import com.atlassian.onetime.model.Issuer
import com.atlassian.onetime.model.TOTPSecret
import com.atlassian.onetime.service.DefaultTOTPService
import com.atlassian.onetime.service.SecretProvider
import com.atlassian.onetime.service.TOTPConfiguration
import com.atlassian.onetime.service.TOTPService
import com.atlassian.onetime.service.TOTPVerificationResult
import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import java.io.ByteArrayOutputStream
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.Base64
import org.springframework.stereotype.Service

@Service
class TotpService(
    private val secretProvider: SecretProvider,
    private val qrCodeWriter: QRCodeWriter,
    private val totpGenerator: TOTPGenerator,
    private val totpConfiguration: TOTPConfiguration,
    private val clock: Clock,
) : TOTPService by DefaultTOTPService(totpGenerator, totpConfiguration) {
    fun generateSecret(): String {
        return secretProvider.generateSecret().base32Encoded
    }

    fun generateQrCodeDataUri(username: String, secret: String): String {
        val url =
            generateTOTPUrl(
                TOTPSecret.fromBase32EncodedString(secret),
                EmailAddress(username),
                Issuer(ISSUER),
            )

        val matrix = qrCodeWriter.encode(url.toString(), BarcodeFormat.QR_CODE, 200, 200)

        val image =
            ByteArrayOutputStream().use {
                MatrixToImageWriter.writeToStream(matrix, "PNG", it)
                it.toByteArray()
            }

        return "data:image/png;base64,${Base64.getEncoder().encodeToString(image)}"
    }

    fun verifyCode(secret: String?, code: String?): Boolean =
        findMatchingStepOrNull(secret, code) != null

    fun findMatchingStepOrNull(secret: String?, code: String?): Long? {
        if (secret.isNullOrBlank() || code.isNullOrBlank()) return null
        val totp = TOTP(code.replace(WHITESPACE_OR_DASH, ""))
        val totpSecret = TOTPSecret.fromBase32EncodedString(secret)

        // Verify against a fixed instant so the matched index and the current step agree
        val now = clock.instant()
        val result = verifierAt(now).verify(totp, totpSecret)
        return if (result is TOTPVerificationResult.Success) stepAt(now) + result.index else null
    }

    private fun verifierAt(instant: Instant): TOTPService =
        DefaultTOTPService(
            TOTPGenerator(
                clock = Clock.fixed(instant, ZoneOffset.UTC),
                startTime = totpGenerator.startTime,
                timeStepSeconds = totpGenerator.timeStepSeconds,
                otpLength = totpGenerator.otpLength,
                digest = totpGenerator.digest,
            ),
            totpConfiguration,
        )

    private fun stepAt(instant: Instant): Long =
        (instant.epochSecond - totpGenerator.startTime) / totpGenerator.timeStepSeconds

    companion object {
        private const val ISSUER = "ksbp-security-mfa-totp"
        private val WHITESPACE_OR_DASH = Regex("[\\s-]")
    }
}
