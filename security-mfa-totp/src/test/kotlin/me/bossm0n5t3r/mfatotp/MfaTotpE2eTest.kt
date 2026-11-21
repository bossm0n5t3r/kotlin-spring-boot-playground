package me.bossm0n5t3r.mfatotp

import java.time.Duration
import kotlin.test.Test
import kotlin.uuid.Uuid
import me.bossm0n5t3r.mfatotp.support.AuthFlow
import me.bossm0n5t3r.mfatotp.support.MutableClock
import me.bossm0n5t3r.mfatotp.support.MutableClockConfiguration
import org.hamcrest.Matchers.containsString
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
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
@Import(MutableClockConfiguration::class, AuthFlow::class)
class MfaTotpE2eTest {
    @Autowired private lateinit var mockMvc: MockMvc
    @Autowired private lateinit var authFlow: AuthFlow
    @Autowired private lateinit var clock: MutableClock

    @Test
    fun user_reaches_dashboard_through_totp_setup_and_again_through_totp_verify_after_relogin() {
        val username = Uuid.random().toHexDashString()

        mockMvc
            .perform(
                post("/signup")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("username", username)
                    .param("password", AuthFlow.PASSWORD)
                    .with(csrf()),
            )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/login"))

        val firstSession = loginAndExpectRedirect(username, "/totp/setup")
        mockMvc
            .perform(get("/totp/setup").session(firstSession))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("data:image/png;base64,")))
        val secret = authFlow.secretOf(username)
        mockMvc
            .perform(authFlow.setupVerifyRequest(firstSession, authFlow.generateCode(secret)))
            .andExpect(status().isSeeOther)
            .andExpect(redirectedUrl("/dashboard"))
        expectDashboardShows(firstSession, username)

        mockMvc
            .perform(post("/logout").session(firstSession).with(csrf()))
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/?logout"))
        mockMvc
            .perform(get("/dashboard").session(firstSession))
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/login"))

        // Use a code from a different time step than the one used for setup
        clock.advance(Duration.ofSeconds(30))
        val secondSession = loginAndExpectRedirect(username, "/totp/verify")
        mockMvc.perform(get("/totp/verify").session(secondSession)).andExpect(status().isOk)
        mockMvc
            .perform(authFlow.verifyRequest(secondSession, authFlow.generateCode(secret)))
            .andExpect(status().isSeeOther)
            .andExpect(redirectedUrl("/dashboard"))
        expectDashboardShows(secondSession, username)
    }

    private fun loginAndExpectRedirect(username: String, expectedUrl: String): MockHttpSession =
        mockMvc
            .perform(
                post("/login")
                    .param("username", username)
                    .param("password", AuthFlow.PASSWORD)
                    .with(csrf()),
            )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl(expectedUrl))
            .andReturn()
            .request
            .getSession(false) as MockHttpSession

    private fun expectDashboardShows(session: MockHttpSession, username: String) {
        mockMvc
            .perform(get("/dashboard").session(session))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString(username)))
    }
}
