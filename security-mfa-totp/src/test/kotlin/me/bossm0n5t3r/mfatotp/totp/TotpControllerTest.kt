package me.bossm0n5t3r.mfatotp.totp

import com.atlassian.onetime.core.TOTPGenerator
import com.atlassian.onetime.model.TOTPSecret
import java.time.Duration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.uuid.Uuid
import me.bossm0n5t3r.mfatotp.support.MutableClock
import me.bossm0n5t3r.mfatotp.support.MutableClockConfiguration
import me.bossm0n5t3r.mfatotp.user.TotpUserDetails
import me.bossm0n5t3r.mfatotp.user.User
import me.bossm0n5t3r.mfatotp.user.UserService
import me.bossm0n5t3r.mfatotp.user.UserStore
import me.bossm0n5t3r.mfatotp.user.dto.CreateUserDto
import org.hamcrest.Matchers.containsString
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.mock.web.MockHttpSession
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@Import(MutableClockConfiguration::class)
class TotpControllerTest {
    @Autowired private lateinit var mockMvc: MockMvc
    @Autowired private lateinit var userService: UserService
    @Autowired private lateinit var userStore: UserStore
    @Autowired private lateinit var totpGenerator: TOTPGenerator
    @Autowired private lateinit var clock: MutableClock

    @Test
    fun setup_issues_secret_and_qr_code_and_keeps_same_secret_on_refresh() {
        // given
        val username = signUp()
        val session = login(username)

        // when
        mockMvc
            .perform(get("/totp/setup").session(session))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("data:image/png;base64,")))
        val secret = assertNotNull(userOf(username).totpSecret)

        // then
        mockMvc
            .perform(get("/totp/setup").session(session))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString(secret)))
        assertEquals(secret, userOf(username).totpSecret)
    }

    @Test
    fun setupVerify_enables_mfa_and_redirects_to_dashboard_if_code_is_correct() {
        // given
        val username = signUp()
        val session = login(username)
        val secret = issueSecret(session, username)

        // when, then
        mockMvc
            .perform(setupVerifyRequest(session, generateCode(secret)))
            .andExpect(status().isSeeOther)
            .andExpect(redirectedUrl("/dashboard"))
        assertTrue(userOf(username).mfaEnabled)
    }

    @Test
    fun setupVerify_returns_200_with_error_message_if_code_is_wrong() {
        // given
        val username = signUp()
        val session = login(username)
        val secret = issueSecret(session, username)

        // when, then
        mockMvc
            .perform(setupVerifyRequest(session, wrongCodeOf(generateCode(secret))))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString(INVALID_CODE_MESSAGE)))
        assertFalse(userOf(username).mfaEnabled)
    }

    @Test
    fun setup_redirects_to_totp_verify_if_mfa_is_already_enabled() {
        // given
        val username = signUpWithMfaEnabled()
        val session = login(username)

        // when, then
        mockMvc
            .perform(get("/totp/setup").session(session))
            .andExpect(status().isSeeOther)
            .andExpect(redirectedUrl("/totp/verify"))
    }

    @Test
    fun setupVerify_returns_403_if_mfa_is_already_enabled() {
        // given
        val username = signUpWithMfaEnabled()
        val session = login(username)
        val secret = assertNotNull(userOf(username).totpSecret)

        // when, then
        mockMvc
            .perform(setupVerifyRequest(session, generateCode(secret)))
            .andExpect(status().isForbidden)
    }

    @Test
    fun verify_redirects_to_dashboard_and_dashboard_shows_username_if_code_is_correct() {
        // given
        val username = signUpWithMfaEnabled()
        val session = login(username)
        val secret = assertNotNull(userOf(username).totpSecret)

        // when
        mockMvc
            .perform(verifyRequest(session, generateCode(secret)))
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
        val username = signUpWithMfaEnabled()
        val session = login(username)
        val secret = assertNotNull(userOf(username).totpSecret)

        // when, then
        mockMvc
            .perform(verifyRequest(session, wrongCodeOf(generateCode(secret))))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString(INVALID_CODE_MESSAGE)))
    }

    @Test
    fun verify_invalidates_session_and_redirects_to_login_locked_after_3_failures() {
        // given
        val username = signUpWithMfaEnabled()
        val session = login(username)
        val secret = assertNotNull(userOf(username).totpSecret)
        failVerify(session, secret, times = 3)

        // when, then
        mockMvc
            .perform(verifyRequest(session, generateCode(secret)))
            .andExpect(status().isSeeOther)
            .andExpect(redirectedUrl("/login?locked"))
        assertTrue(session.isInvalid)
    }

    @Test
    fun verify_rejects_correct_code_after_relogin_if_locked() {
        // given
        val username = signUpWithMfaEnabled()
        val secret = assertNotNull(userOf(username).totpSecret)
        failVerify(login(username), secret, times = 3)
        val newSession = login(username)

        // when, then
        mockMvc
            .perform(verifyRequest(newSession, generateCode(secret)))
            .andExpect(status().isSeeOther)
            .andExpect(redirectedUrl("/login?locked"))
    }

    @Test
    fun verify_accepts_correct_code_if_1_hour_has_passed_since_lock() {
        // given
        val username = signUpWithMfaEnabled()
        val secret = assertNotNull(userOf(username).totpSecret)
        failVerify(login(username), secret, times = 3)
        clock.advance(Duration.ofHours(1))
        val newSession = login(username)

        // when, then
        mockMvc
            .perform(verifyRequest(newSession, generateCode(secret)))
            .andExpect(status().isSeeOther)
            .andExpect(redirectedUrl("/dashboard"))
    }

    @Test
    fun verify_resets_failure_count_if_code_is_correct() {
        // given
        val username = signUpWithMfaEnabled()
        val secret = assertNotNull(userOf(username).totpSecret)
        val session = login(username)
        failVerify(session, secret, times = 2)
        mockMvc
            .perform(verifyRequest(session, generateCode(secret)))
            .andExpect(redirectedUrl("/dashboard"))
        val newSession = login(username)

        // when
        failVerify(newSession, secret, times = 2)

        // then
        mockMvc
            .perform(verifyRequest(newSession, generateCode(secret)))
            .andExpect(status().isSeeOther)
            .andExpect(redirectedUrl("/dashboard"))
    }

    @Test
    fun setupVerify_and_verify_share_failure_count() {
        // given
        val username = signUp()
        val session = login(username)
        val secret = issueSecret(session, username)
        repeat(2) {
            mockMvc
                .perform(setupVerifyRequest(session, wrongCodeOf(generateCode(secret))))
                .andExpect(status().isOk)
        }
        failVerify(session, secret, times = 1)

        // when, then
        mockMvc
            .perform(setupVerifyRequest(session, generateCode(secret)))
            .andExpect(status().isSeeOther)
            .andExpect(redirectedUrl("/login?locked"))
    }

    private fun failVerify(session: MockHttpSession, secret: String, times: Int) {
        repeat(times) {
            mockMvc
                .perform(verifyRequest(session, wrongCodeOf(generateCode(secret))))
                .andExpect(status().isOk)
                .andExpect(content().string(containsString(INVALID_CODE_MESSAGE)))
        }
    }

    private fun signUp(): String {
        val username = Uuid.random().toHexDashString()
        userService.createUser(CreateUserDto(username = username, password = PASSWORD))
        return username
    }

    private fun signUpWithMfaEnabled(): String {
        val username = signUp()
        val session = login(username)
        val secret = issueSecret(session, username)
        mockMvc
            .perform(setupVerifyRequest(session, generateCode(secret)))
            .andExpect(status().isSeeOther)
        return username
    }

    private fun login(username: String): MockHttpSession {
        val result =
            mockMvc
                .perform(
                    post("/login")
                        .param("username", username)
                        .param("password", PASSWORD)
                        .with(csrf()),
                )
                .andExpect(status().is3xxRedirection)
                .andReturn()
        return result.request.getSession(false) as MockHttpSession
    }

    private fun issueSecret(session: MockHttpSession, username: String): String {
        mockMvc.perform(get("/totp/setup").session(session)).andExpect(status().isOk)
        return assertNotNull(userOf(username).totpSecret)
    }

    private fun userOf(username: String): User =
        (userStore.loadUserByUsername(username) as TotpUserDetails).user

    private fun generateCode(secret: String): String =
        totpGenerator.generateCurrent(TOTPSecret.fromBase32EncodedString(secret)).value

    private fun wrongCodeOf(code: String): String {
        val chars = code.toCharArray()
        chars[0] = if (chars[0] == '0') '1' else '0'
        return String(chars)
    }

    private fun setupVerifyRequest(session: MockHttpSession, code: String) =
        post("/totp/setup/verify").session(session).param("code", code).with(csrf())

    private fun verifyRequest(session: MockHttpSession, code: String) =
        post("/totp/verify").session(session).param("code", code).with(csrf())

    companion object {
        private const val PASSWORD = "password"
        private const val INVALID_CODE_MESSAGE = "Invalid code. Please try again."
    }
}
