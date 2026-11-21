package me.bossm0n5t3r.mfatotp.support

import com.atlassian.onetime.core.TOTPGenerator
import com.atlassian.onetime.model.TOTPSecret
import java.time.Duration
import kotlin.test.assertNotNull
import kotlin.uuid.Uuid
import me.bossm0n5t3r.mfatotp.user.TotpUserDetails
import me.bossm0n5t3r.mfatotp.user.User
import me.bossm0n5t3r.mfatotp.user.UserService
import me.bossm0n5t3r.mfatotp.user.UserStore
import me.bossm0n5t3r.mfatotp.user.dto.CreateUserDto
import org.springframework.boot.test.context.TestComponent
import org.springframework.mock.web.MockHttpSession
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * Drives the sign-up -> login -> TOTP flow through MockMvc for integration tests.
 *
 * Use with `@Import(MutableClockConfiguration::class, AuthFlow::class)` on a `@SpringBootTest` with
 * `@AutoConfigureMockMvc`.
 */
@TestComponent
class AuthFlow(
    private val mockMvc: MockMvc,
    private val userService: UserService,
    private val userStore: UserStore,
    private val totpGenerator: TOTPGenerator,
    private val clock: MutableClock,
) {
    fun signUp(): String {
        val username = Uuid.random().toHexDashString()
        userService.createUser(CreateUserDto(username = username, password = PASSWORD))
        return username
    }

    fun signUpWithMfaEnabled(): String {
        val username = signUp()
        completeTotpSetup(login(username), username)
        advanceToNextStep()
        return username
    }

    fun login(username: String): MockHttpSession =
        mockMvc
            .perform(
                post("/login").param("username", username).param("password", PASSWORD).with(csrf()),
            )
            .andExpect(status().is3xxRedirection)
            .andReturn()
            .request
            .getSession(false) as MockHttpSession

    fun issueSecret(session: MockHttpSession, username: String): String {
        mockMvc.perform(get("/totp/setup").session(session)).andExpect(status().isOk)
        return assertNotNull(userOf(username).totpSecret)
    }

    fun completeTotpSetup(session: MockHttpSession, username: String) {
        val secret = issueSecret(session, username)
        mockMvc
            .perform(setupVerifyRequest(session, generateCode(secret)))
            .andExpect(status().isSeeOther)
            .andExpect(redirectedUrl("/dashboard"))
    }

    fun fullyAuthenticatedSession(): MockHttpSession {
        val username = signUp()
        val session = login(username)
        completeTotpSetup(session, username)
        return session
    }

    fun advanceToNextStep() {
        clock.advance(Duration.ofSeconds(totpGenerator.timeStepSeconds.toLong()))
    }

    fun userOf(username: String): User =
        (userStore.loadUserByUsername(username) as TotpUserDetails).user

    fun secretOf(username: String): String = assertNotNull(userOf(username).totpSecret)

    fun generateCode(secret: String): String =
        totpGenerator.generateCurrent(TOTPSecret.fromBase32EncodedString(secret)).value

    fun wrongCodeOf(code: String): String {
        val chars = code.toCharArray()
        chars[0] = if (chars[0] == '0') '1' else '0'
        return String(chars)
    }

    fun setupVerifyRequest(session: MockHttpSession, code: String): MockHttpServletRequestBuilder =
        post("/totp/setup/verify").session(session).param("code", code).with(csrf())

    fun verifyRequest(session: MockHttpSession, code: String): MockHttpServletRequestBuilder =
        post("/totp/verify").session(session).param("code", code).with(csrf())

    companion object {
        const val PASSWORD = "password"
    }
}
