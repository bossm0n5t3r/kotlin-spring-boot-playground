package me.bossm0n5t3r.mfatotp.configuration

import kotlin.test.Test
import me.bossm0n5t3r.mfatotp.support.AuthFlow
import me.bossm0n5t3r.mfatotp.support.MutableClockConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@Import(MutableClockConfiguration::class, AuthFlow::class)
class LogoutTest {
    @Autowired private lateinit var mockMvc: MockMvc
    @Autowired private lateinit var authFlow: AuthFlow

    @Test
    fun logout_redirects_to_home_with_logout_param() {
        // given
        val session = authFlow.fullyAuthenticatedSession()

        // when, then
        mockMvc
            .perform(post("/logout").session(session).with(csrf()))
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/?logout"))
    }

    @Test
    fun logout_blocks_dashboard_access_afterwards() {
        // given
        val session = authFlow.fullyAuthenticatedSession()
        mockMvc.perform(get("/dashboard").session(session)).andExpect(status().isOk)

        // when
        mockMvc.perform(post("/logout").session(session).with(csrf()))

        // then
        mockMvc
            .perform(get("/dashboard").session(session))
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/login"))
    }
}
