package me.bossm0n5t3r.mfatotp.web

import kotlin.test.Test
import kotlin.uuid.Uuid
import me.bossm0n5t3r.mfatotp.user.UserService
import me.bossm0n5t3r.mfatotp.user.dto.CreateUserDto
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.not
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class LoginControllerTest {
    @Autowired private lateinit var mockMvc: MockMvc
    @Autowired private lateinit var userService: UserService

    @Test
    fun post_redirects_to_login_error_if_password_is_wrong() {
        // given
        val username = Uuid.random().toHexDashString()
        userService.createUser(CreateUserDto(username = username, password = "password"))

        // when, then
        mockMvc
            .perform(
                post("/login")
                    .param("username", username)
                    .param("password", "wrong-password")
                    .with(csrf()),
            )
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/login?error"))
    }

    @Test
    fun login_shows_invalid_credentials_message_if_error_param_is_present() {
        mockMvc
            .perform(get("/login").param("error", ""))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString(INVALID_CREDENTIALS_MESSAGE)))
    }

    @Test
    fun login_shows_logout_message_if_logout_param_is_present() {
        mockMvc
            .perform(get("/login").param("logout", ""))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString(LOGOUT_MESSAGE)))
    }

    @Test
    fun login_shows_locked_message_if_locked_param_is_present() {
        mockMvc
            .perform(get("/login").param("locked", ""))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString(LOCKED_MESSAGE)))
    }

    @Test
    fun login_shows_only_locked_message_if_locked_and_error_params_are_present() {
        mockMvc
            .perform(get("/login").param("locked", "").param("error", ""))
            .andExpect(status().isOk)
            .andExpect(content().string(containsString(LOCKED_MESSAGE)))
            .andExpect(content().string(not(containsString(INVALID_CREDENTIALS_MESSAGE))))
    }

    companion object {
        private const val INVALID_CREDENTIALS_MESSAGE = "Invalid username or password"
        private const val LOGOUT_MESSAGE = "You have been logged out"
        private const val LOCKED_MESSAGE = "Too many failed attempts. Please try again later."
    }
}
