package me.bossm0n5t3r.mfatotp.totp

import com.atlassian.onetime.core.HMACDigest
import com.atlassian.onetime.core.OTPLength
import com.atlassian.onetime.core.TOTPGenerator
import com.atlassian.onetime.model.TOTPSecret
import com.atlassian.onetime.service.RandomSecretProvider
import com.atlassian.onetime.service.TOTPConfiguration
import com.google.zxing.BinaryBitmap
import com.google.zxing.client.j2se.BufferedImageLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import com.google.zxing.qrcode.QRCodeWriter
import java.time.Duration
import java.util.Base64
import java.util.stream.Stream
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import me.bossm0n5t3r.mfatotp.support.MutableClock
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.NullAndEmptySource
import org.junit.jupiter.params.provider.ValueSource

class TotpServiceTest {
    private val totpProperties =
        TotpProperties(
            allowed = TotpProperties.Allowed(pastSteps = 1, futureSteps = 1),
            startTime = 0,
            timeStepSeconds = 30,
            otpLength = OTPLength.SIX,
            digest = HMACDigest.SHA1,
        )
    private val clock = MutableClock()
    private val secretProvider = RandomSecretProvider()
    private val totpGenerator =
        TOTPGenerator(
            clock = clock,
            startTime = totpProperties.startTime,
            timeStepSeconds = totpProperties.timeStepSeconds,
            otpLength = totpProperties.otpLength,
            digest = totpProperties.digest,
        )
    private val totpConfiguration =
        TOTPConfiguration(
            allowedPastSteps = totpProperties.allowed.pastSteps,
            allowedFutureSteps = totpProperties.allowed.futureSteps,
        )
    private val qrCodeWriter = QRCodeWriter()
    private val sut = TotpService(secretProvider, qrCodeWriter, totpGenerator, totpConfiguration)

    private fun stepsOf(steps: Long): Duration =
        Duration.ofSeconds(totpProperties.timeStepSeconds * steps)

    private fun generateCode(secret: String): String =
        totpGenerator.generateCurrent(TOTPSecret.fromBase32EncodedString(secret)).value

    @Test
    fun verifyCode_returns_true_for_current_code() {
        // given
        val secret = sut.generateSecret()
        val code = generateCode(secret)

        // when
        val result = sut.verifyCode(secret, code)

        // then
        assertTrue { result }
    }

    @Test
    fun verifyCode_returns_false_for_wrong_code() {
        // given
        val secret = "ABCDEFGHIJKLMNOP"
        val code = generateCode(secret).toCharArray()
        code[0] = if (code[0] == '0') '1' else '0'
        val wrongCode = String(code)

        // when
        val result = sut.verifyCode(secret, wrongCode)

        // then
        assertFalse { result }
    }

    @Test
    fun verifyCode_returns_true_if_code_is_within_past_steps() {
        // given
        val secret = sut.generateSecret()
        val code = generateCode(secret)
        clock.advance(stepsOf(totpProperties.allowed.pastSteps.toLong()))

        // when
        val result = sut.verifyCode(secret, code)

        // then
        assertTrue { result }
    }

    @Test
    fun verifyCode_returns_false_if_code_is_beyond_past_steps() {
        // given
        val secret = sut.generateSecret()
        val code = generateCode(secret)
        clock.advance(stepsOf(totpProperties.allowed.pastSteps + 1L))

        // when
        val result = sut.verifyCode(secret, code)

        // then
        assertFalse { result }
    }

    @Test
    fun verifyCode_returns_true_if_code_is_within_future_steps() {
        // given
        val secret = sut.generateSecret()
        val steps = stepsOf(totpProperties.allowed.futureSteps.toLong())
        clock.advance(steps)
        val code = generateCode(secret)
        clock.advance(steps.negated())

        // when
        val result = sut.verifyCode(secret, code)

        // then
        assertTrue { result }
    }

    @Test
    fun verifyCode_returns_false_if_code_is_beyond_future_steps() {
        // given
        val secret = sut.generateSecret()
        val steps = stepsOf(totpProperties.allowed.futureSteps + 1L)
        clock.advance(steps)
        val code = generateCode(secret)
        clock.advance(steps.negated())

        // when
        val result = sut.verifyCode(secret, code)

        // then
        assertFalse { result }
    }

    @Test
    fun verifyCode_normalizes_spaces_and_hyphens_in_code() {
        // given
        val secret = sut.generateSecret()
        val code = generateCode(secret)
        val weirdCodes =
            listOf(
                "$code ",
                " $code",
                " $code ",
                code.split("").joinToString(" "),
                "$code-",
                "-$code",
                "-$code-",
                code.split("").joinToString("-"),
            )

        // when, then
        weirdCodes.forEach { weirdCode ->
            val result = sut.verifyCode(secret, weirdCode)

            assertTrue("weirdCode=`$weirdCode`") { result }
        }
    }

    @ParameterizedTest(name = "secret=`{0}`, code=`{1}`")
    @MethodSource("secretAndCodeAreNullOrBlank")
    fun verifyCode_returns_false_if_secret_and_code_are_null_or_blank(
        secret: String?,
        code: String?,
    ) {
        // when, then
        val result = sut.verifyCode(secret, code)

        assertFalse { result }
    }

    @ParameterizedTest(name = "code=`{0}`")
    @NullAndEmptySource
    @ValueSource(strings = [" "])
    fun verifyCode_returns_false_if_code_is_null_or_blank(code: String?) {
        // given
        val secret = sut.generateSecret()

        // when
        val result = sut.verifyCode(secret, code)

        // then
        assertFalse { result }
    }

    @ParameterizedTest(name = "secret=`{0}`")
    @NullAndEmptySource
    @ValueSource(strings = [" "])
    fun verifyCode_returns_false_if_secret_is_null_or_blank(secret: String?) {
        // given
        val code = generateCode(sut.generateSecret())

        // when
        val result = sut.verifyCode(secret, code)

        // then
        assertFalse { result }
    }

    @Test
    fun generateSecret_returns_unique_base32_secret() {
        // when
        val firstSecret = sut.generateSecret()
        val secondSecret = sut.generateSecret()

        // then
        assertTrue { firstSecret.matches(BASE32_REGEX) }
        assertTrue { secondSecret.matches(BASE32_REGEX) }
        assertTrue { firstSecret != secondSecret }
    }

    @Test
    fun generateQrCodeDataUri_returns_png_data_uri() {
        // given
        val username = "anonymous"
        val secret = sut.generateSecret()

        // when
        val qrCodeDataUri = sut.generateQrCodeDataUri(username, secret)

        // then
        assertTrue { qrCodeDataUri.startsWith("data:image/png;base64,") }
    }

    @Test
    fun generateQrCodeDataUri_encodes_otpauth_url_with_secret_username_and_issuer() {
        // given
        val username = "anonymous"
        val secret = sut.generateSecret()

        // when
        val qrCodeDataUri = sut.generateQrCodeDataUri(username, secret)

        // then
        val png = Base64.getDecoder().decode(qrCodeDataUri.removePrefix("data:image/png;base64,"))
        val image = assertNotNull(ImageIO.read(png.inputStream()))
        val bitmap = BinaryBitmap(HybridBinarizer(BufferedImageLuminanceSource(image)))
        val url = QRCodeReader().decode(bitmap).text

        assertTrue("url=`$url`") { url.startsWith("otpauth://totp/") }
        assertTrue("url=`$url`") { url.contains("secret=$secret") }
        assertTrue("url=`$url`") { url.contains(username) }
        assertTrue("url=`$url`") { url.contains("issuer=ksbp-security-mfa-totp") }
    }

    companion object {
        private val BASE32_REGEX = Regex("^[A-Z2-7]+=*$")

        @JvmStatic
        fun secretAndCodeAreNullOrBlank(): Stream<Arguments> =
            Stream.of(
                Arguments.of(null, null),
                Arguments.of(null, ""),
                Arguments.of(null, " "),
                Arguments.of("", null),
                Arguments.of("", ""),
                Arguments.of("", " "),
                Arguments.of(" ", null),
                Arguments.of(" ", ""),
                Arguments.of(" ", " "),
            )
    }
}
