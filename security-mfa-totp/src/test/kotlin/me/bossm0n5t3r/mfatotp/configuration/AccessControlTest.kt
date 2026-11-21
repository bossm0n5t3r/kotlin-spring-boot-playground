package me.bossm0n5t3r.mfatotp.configuration

import me.bossm0n5t3r.mfatotp.user.TotpUserDetails
import me.bossm0n5t3r.mfatotp.user.User
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.FactorGrantedAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class AccessControlTest {
    @Autowired private lateinit var mockMvc: MockMvc

    @ParameterizedTest(name = "path=`{0}`")
    @ValueSource(strings = ["/", "/signup", "/login", "/css/main.css"])
    fun get_returns_200_for_public_pages_if_anonymous(path: String) {
        mockMvc.perform(get(path)).andExpect(status().isOk)
    }

    @ParameterizedTest(name = "path=`{0}`")
    @ValueSource(strings = ["/totp/setup", "/totp/verify", "/dashboard"])
    fun get_redirects_to_login_for_protected_pages_if_anonymous(path: String) {
        mockMvc
            .perform(get(path))
            .andExpect(status().is3xxRedirection)
            .andExpect(redirectedUrl("/login"))
    }

    @ParameterizedTest(name = "path=`{0}`")
    @ValueSource(strings = ["/totp/setup", "/totp/verify"])
    fun get_returns_200_for_totp_pages_if_only_password_factor_is_granted(path: String) {
        mockMvc.perform(get(path).with(passwordOnlyAuthentication())).andExpect(status().isOk)
    }

    @ParameterizedTest(name = "path=`{0}`")
    @ValueSource(strings = ["/dashboard"])
    fun get_returns_403_for_dashboard_if_only_password_factor_is_granted(path: String) {
        mockMvc
            .perform(get(path).with(passwordOnlyAuthentication()))
            .andExpect(status().isForbidden)
    }

    @ParameterizedTest(name = "path=`{0}`")
    @ValueSource(strings = ["/signup", "/login"])
    fun post_returns_403_without_csrf_token_if_anonymous(path: String) {
        mockMvc
            .perform(post(path).param("username", "alice").param("password", "password"))
            .andExpect(status().isForbidden)
    }

    @ParameterizedTest(name = "path=`{0}`")
    @ValueSource(strings = ["/totp/setup/verify", "/totp/verify"])
    fun post_returns_403_without_csrf_token_if_only_password_factor_is_granted(path: String) {
        mockMvc
            .perform(post(path).param("code", "123456").with(passwordOnlyAuthentication()))
            .andExpect(status().isForbidden)
    }

    private fun passwordOnlyAuthentication() =
        authentication(
            TotpUserDetails(User("alice", "password")).let { userDetails ->
                UsernamePasswordAuthenticationToken.authenticated(
                    userDetails,
                    null,
                    userDetails.authorities + FactorGrantedAuthority.fromFactor("PASSWORD"),
                )
            },
        )
}
