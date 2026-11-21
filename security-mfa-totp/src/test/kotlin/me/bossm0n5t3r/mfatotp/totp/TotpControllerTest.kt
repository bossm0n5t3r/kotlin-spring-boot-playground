package me.bossm0n5t3r.mfatotp.totp

import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import me.bossm0n5t3r.mfatotp.support.AuthFlow
import me.bossm0n5t3r.mfatotp.support.MutableClock
import me.bossm0n5t3r.mfatotp.support.MutableClockConfiguration
import org.hamcrest.Matchers.containsString
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.mock.web.MockHttpSession
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@Import(MutableClockConfiguration::class, AuthFlow::class)
class TotpControllerTest {
    @Autowired private lateinit var mockMvc: MockMvc
    @Autowired private lateinit var authFlow: AuthFlow
    @Autowired private lateinit var clock: MutableClock

    @Test
    fun setup_issues_secret_and_qr_code_and_keeps_same_secret_on_refresh() {
        // given
        val username = authFlow.signUp()
        val session = authFlow.login(username)

        // when
        mockMvc
            .perform(get("/totp/setup").session(session))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("data:image/png;base64,")))
        val secret = authFlow.secretOf(username)

        // then
        mockMvc
            .perform(get("/totp/setup").session(session))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString(secret)))
        assertEquals(secret, authFlow.userOf(username).totpSecret)
    }

    @Test
    fun setupVerify_enables_mfa_and_redirects_to_dashboard_if_code_is_correct() {
        // given
        val username = authFlow.signUp()
        val session = authFlow.login(username)
        val secret = authFlow.issueSecret(session, username)

        // when, then
        mockMvc
            .perform(authFlow.setupVerifyRequest(session, authFlow.generateCode(secret)))
            .andExpect(status().isSeeOther)
            .andExpect(redirectedUrl("/dashboard"))
        assertTrue(authFlow.userOf(username).mfaEnabled)
    }

    @Test
    fun setupVerify_returns_200_with_error_message_if_code_is_wrong() {
        // given
        val username = authFlow.signUp()
        val session = authFlow.login(username)
        val secret = authFlow.issueSecret(session, username)

        // when, then
        mockMvc
            .perform(
                authFlow.setupVerifyRequest(
                    session,
                    authFlow.wrongCodeOf(authFlow.generateCode(secret)),
                ),
            )
            .andExpect(status().isOk)
            .andExpect(content().string(containsString(INVALID_CODE_MESSAGE)))
        assertFalse(authFlow.userOf(username).mfaEnabled)
    }

    @Test
    fun setup_redirects_to_totp_verify_if_mfa_is_already_enabled() {
        // given
        val username = authFlow.signUpWithMfaEnabled()
        val session = authFlow.login(username)

        // when, then
        mockMvc
            .perform(get("/totp/setup").session(session))
            .andExpect(status().isSeeOther)
            .andExpect(redirectedUrl("/totp/verify"))
    }

    @Test
    fun setupVerify_returns_403_if_mfa_is_already_enabled() {
        // given
        val username = authFlow.signUpWithMfaEnabled()
        val session = authFlow.login(username)
        val secret = authFlow.secretOf(username)

        // when, then
        mockMvc
            .perform(authFlow.setupVerifyRequest(session, authFlow.generateCode(secret)))
            .andExpect(status().isForbidden)
    }

    @Test
    fun verify_redirects_to_dashboard_and_dashboard_shows_username_if_code_is_correct() {
        // given
        val username = authFlow.signUpWithMfaEnabled()
        val session = authFlow.login(username)
        val secret = authFlow.secretOf(username)

        // when
        mockMvc
            .perform(authFlow.verifyRequest(session, authFlow.generateCode(secret)))
            .andExpect(status().isSeeOther)
            .andExpect(redirectedUrl("/dashboard"))

        // then
        mockMvc
            .perform(get("/dashboard").session(session))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString(username)))
    }

    @Test
    fun verify_returns_200_with_error_message_if_code_is_wrong() {
        // given
        val username = authFlow.signUpWithMfaEnabled()
        val session = authFlow.login(username)
        val secret = authFlow.secretOf(username)

        // when, then
        mockMvc
            .perform(
                authFlow.verifyRequest(
                    session,
                    authFlow.wrongCodeOf(authFlow.generateCode(secret)),
                ),
            )
            .andExpect(status().isOk)
            .andExpect(content().string(containsString(INVALID_CODE_MESSAGE)))
    }

    @Test
    fun verify_invalidates_session_and_redirects_to_login_locked_after_3_failures() {
        // given
        val username = authFlow.signUpWithMfaEnabled()
        val session = authFlow.login(username)
        val secret = authFlow.secretOf(username)
        failVerify(session, secret, times = 3)

        // when, then
        mockMvc
            .perform(authFlow.verifyRequest(session, authFlow.generateCode(secret)))
            .andExpect(status().isSeeOther)
            .andExpect(redirectedUrl("/login?locked"))
        assertTrue(session.isInvalid)
    }

    @Test
    fun verify_rejects_correct_code_after_relogin_if_locked() {
        // given
        val username = authFlow.signUpWithMfaEnabled()
        val secret = authFlow.secretOf(username)
        failVerify(authFlow.login(username), secret, times = 3)
        val newSession = authFlow.login(username)

        // when, then
        mockMvc
            .perform(authFlow.verifyRequest(newSession, authFlow.generateCode(secret)))
            .andExpect(status().isSeeOther)
            .andExpect(redirectedUrl("/login?locked"))
    }

    @Test
    fun verify_accepts_correct_code_if_1_hour_has_passed_since_lock() {
        // given
        val username = authFlow.signUpWithMfaEnabled()
        val secret = authFlow.secretOf(username)
        failVerify(authFlow.login(username), secret, times = 3)
        clock.advance(Duration.ofHours(1))
        val newSession = authFlow.login(username)

        // when, then
        mockMvc
            .perform(authFlow.verifyRequest(newSession, authFlow.generateCode(secret)))
            .andExpect(status().isSeeOther)
            .andExpect(redirectedUrl("/dashboard"))
    }

    @Test
    fun verify_resets_failure_count_if_code_is_correct() {
        // given
        val username = authFlow.signUpWithMfaEnabled()
        val secret = authFlow.secretOf(username)
        val session = authFlow.login(username)
        failVerify(session, secret, times = 2)
        mockMvc
            .perform(authFlow.verifyRequest(session, authFlow.generateCode(secret)))
            .andExpect(redirectedUrl("/dashboard"))
        val newSession = authFlow.login(username)

        // when
        failVerify(newSession, secret, times = 2)

        // then
        mockMvc
            .perform(authFlow.verifyRequest(newSession, authFlow.generateCode(secret)))
            .andExpect(status().isSeeOther)
            .andExpect(redirectedUrl("/dashboard"))
    }

    @Test
    fun setupVerify_and_verify_share_failure_count() {
        // given
        val username = authFlow.signUp()
        val session = authFlow.login(username)
        val secret = authFlow.issueSecret(session, username)
        repeat(2) {
            mockMvc
                .perform(
                    authFlow.setupVerifyRequest(
                        session,
                        authFlow.wrongCodeOf(authFlow.generateCode(secret)),
                    ),
                )
                .andExpect(status().isOk)
        }
        failVerify(session, secret, times = 1)

        // when, then
        mockMvc
            .perform(authFlow.setupVerifyRequest(session, authFlow.generateCode(secret)))
            .andExpect(status().isSeeOther)
            .andExpect(redirectedUrl("/login?locked"))
    }

    private fun failVerify(session: MockHttpSession, secret: String, times: Int) {
        repeat(times) {
            mockMvc
                .perform(
                    authFlow.verifyRequest(
                        session,
                        authFlow.wrongCodeOf(authFlow.generateCode(secret)),
                    ),
                )
                .andExpect(status().isOk)
                .andExpect(content().string(containsString(INVALID_CODE_MESSAGE)))
        }
    }

    companion object {
        private const val INVALID_CODE_MESSAGE = "Invalid code. Please try again."
    }
}
