package me.bossm0n5t3r.mfatotp.configuration

import com.atlassian.onetime.core.TOTPGenerator
import com.atlassian.onetime.model.TOTPSecret
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.uuid.Uuid
import me.bossm0n5t3r.mfatotp.support.MutableClockConfiguration
import me.bossm0n5t3r.mfatotp.user.TotpUserDetails
import me.bossm0n5t3r.mfatotp.user.UserService
import me.bossm0n5t3r.mfatotp.user.UserStore
import me.bossm0n5t3r.mfatotp.user.dto.CreateUserDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.mock.web.MockHttpSession
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@Import(MutableClockConfiguration::class)
class LogoutTest {
    @Autowired private lateinit var mockMvc: MockMvc
    @Autowired private lateinit var userService: UserService
    @Autowired private lateinit var userStore: UserStore
    @Autowired private lateinit var totpGenerator: TOTPGenerator

    @Test
    fun logout_redirects_to_home_with_logout_param() {
        // given
        val session = fullyAuthenticatedSession()

        // when, then
        mockMvc
            .perform(post("/logout").session(session).with(csrf()))
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/?logout"))
    }

    @Test
    fun logout_blocks_dashboard_access_afterwards() {
        // given
        val session = fullyAuthenticatedSession()
        mockMvc.perform(get("/dashboard").session(session)).andExpect(status().isOk)

        // when
        mockMvc.perform(post("/logout").session(session).with(csrf()))

        // then
        mockMvc
            .perform(get("/dashboard").session(session))
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/login"))
    }

    /** Signs up, logs in and completes TOTP setup, returning a session with `FACTOR_TOTP`. */
    private fun fullyAuthenticatedSession(): MockHttpSession {
        val username = Uuid.random().toHexDashString()
        userService.createUser(CreateUserDto(username = username, password = PASSWORD))

        val session =
            mockMvc
                .perform(
                    post("/login")
                        .param("username", username)
                        .param("password", PASSWORD)
                        .with(csrf()),
                )
                .andExpect(status().is3xxRedirection)
                .andReturn()
                .request
                .getSession(false) as MockHttpSession

        mockMvc.perform(get("/totp/setup").session(session)).andExpect(status().isOk)
        val user = (userStore.loadUserByUsername(username) as TotpUserDetails).user
        val secret = assertNotNull(user.totpSecret)
        val code = totpGenerator.generateCurrent(TOTPSecret.fromBase32EncodedString(secret)).value

        mockMvc
            .perform(
                post("/totp/setup/verify").session(session).param("code", code).with(csrf()),
            )
            .andExpect(status().isSeeOther)
            .andExpect(redirectedUrl("/dashboard"))
        return session
    }

    companion object {
        private const val PASSWORD = "password"
    }
}
