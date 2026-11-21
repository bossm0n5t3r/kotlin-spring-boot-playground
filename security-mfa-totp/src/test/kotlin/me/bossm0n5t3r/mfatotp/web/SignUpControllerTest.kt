package me.bossm0n5t3r.mfatotp.web

import kotlin.test.Test
import kotlin.uuid.Uuid
import org.hamcrest.Matchers.containsString
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class SignUpControllerTest {
    @Autowired private lateinit var mockMvc: MockMvc

    @Test
    fun signUp_redirects_to_login_if_user_is_created() {
        // given
        val username = Uuid.random().toHexDashString()

        // when, then
        mockMvc
            .perform(signUpRequest(username))
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/login"))
    }

    @Test
    fun signUp_redirects_to_signup_error_if_username_already_exists() {
        // given
        val username = Uuid.random().toHexDashString()
        mockMvc.perform(signUpRequest(username))

        // when, then
        mockMvc
            .perform(signUpRequest(username))
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/signup?error"))
    }

    @Test
    fun signUp_shows_username_already_exists_message_if_error_param_is_present() {
        mockMvc
            .perform(get("/signup").param("error", ""))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString("Username already exists")))
    }

    private fun signUpRequest(username: String) =
        post("/signup")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("username", username)
            .param("password", "password")
            .with(csrf())
}
