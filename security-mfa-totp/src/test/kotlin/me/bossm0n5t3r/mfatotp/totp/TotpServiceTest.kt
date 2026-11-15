package me.bossm0n5t3r.mfatotp.totp

import com.atlassian.onetime.core.HMACDigest
import com.atlassian.onetime.core.OTPLength
import com.atlassian.onetime.core.TOTPGenerator
import com.atlassian.onetime.model.TOTPSecret
import com.atlassian.onetime.service.RandomSecretProvider
import com.atlassian.onetime.service.TOTPConfiguration
import com.google.zxing.qrcode.QRCodeWriter
import java.time.Duration
import java.util.stream.Stream
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import me.bossm0n5t3r.mfatotp.support.MutableClock
import org.junit.jupiter.api.DisplayName
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
    @DisplayName("verifyCode - 현재 시각의 올바른 코드는 통과해야 한다")
    fun verifyCodeTest0() {
        // given
        val secret = sut.generateSecret()
        val code = generateCode(secret)

        // when
        val result = sut.verifyCode(secret, code)

        // then
        assertTrue { result }
    }

    @Test
    @DisplayName("verifyCode - 현재 시각의 틀린 코드는 실패해야 한다")
    fun verifyCodeTest1() {
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
    @DisplayName("verifyCode - past-steps 범위 안의 코드는 통과해야 한다")
    fun verifyCodeTest2() {
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
    @DisplayName("verifyCode - past-steps 범위 밖의 코드는 실패해야 한다")
    fun verifyCodeTest3() {
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
    @DisplayName("verifyCode - future-steps 범위 안의 코드는 통과해야 한다")
    fun verifyCodeTest4() {
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
    @DisplayName("verifyCode - future-steps 범위 밖의 코드는 실패해야 한다")
    fun verifyCodeTest5() {
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
    @DisplayName("verifyCode - 공백이나 '-' 가 섞인 코드도 정규화 후 검증해야 한다")
    fun verifyCodeTest6() {
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

            assertTrue { result }
        }
    }

    @ParameterizedTest(name = "secret=`{0}`, code=`{1}`")
    @MethodSource("secretAndCodeAreNullOrBlank")
    @DisplayName("verifyCode - secret 이나 code 가 null 또는 빈 문자열이면 실패해야 한다")
    fun verifyCodeTest7(secret: String?, code: String?) {
        // when, then
        val result = sut.verifyCode(secret, code)

        assertFalse { result }
    }

    @ParameterizedTest(name = "code=`{0}`")
    @NullAndEmptySource
    @ValueSource(strings = [" "])
    @DisplayName("verifyCode - secret 이 유효해도 code 가 null 또는 빈 문자열이면 실패해야 한다")
    fun verifyCodeTest8(code: String?) {
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
    @DisplayName("verifyCode - code 가 유효해도 secret 이 null 또는 빈 문자열이면 실패해야 한다")
    fun verifyCodeTest9(secret: String?) {
        // given
        val code = generateCode(sut.generateSecret())

        // when
        val result = sut.verifyCode(secret, code)

        // then
        assertFalse { result }
    }

    @Test
    @DisplayName("generateSecret - Base32 secret 을 생성하고 호출마다 다른 값을 반환해야 한다")
    fun generateSecretTest() {
        // when
        val firstSecret = sut.generateSecret()
        val secondSecret = sut.generateSecret()

        // then
        assertTrue { firstSecret.matches(BASE32_REGEX) }
        assertTrue { secondSecret.matches(BASE32_REGEX) }
        assertTrue { firstSecret != secondSecret }
    }

    @Test
    @DisplayName("generateQrCodeDataUri - 'data:image/png;base64,' 형식을 반환해야 한다")
    fun generateQrCodeDataUriTest() {
        // given
        val username = "anonymous"
        val secret = sut.generateSecret()

        // when
        val qrCodeDataUri = sut.generateQrCodeDataUri(username, secret)

        // then
        assertTrue { qrCodeDataUri.startsWith("data:image/png;base64,") }
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
