package me.bossm0n5t3r.mfatotp.web

import kotlin.test.Test
import me.bossm0n5t3r.mfatotp.totp.TotpAuthenticationHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class HomeControllerTest {
    @Autowired private lateinit var mockMvc: MockMvc

    @Test
    fun dashboard_returns_403_if_principal_is_not_totp_user_details() {
        mockMvc
            .perform(
                get("/dashboard")
                    .with(
                        user("alice")
                            .authorities(
                                SimpleGrantedAuthority(TotpAuthenticationHandler.TOTP_AUTHORITY),
                            ),
                    ),
            )
            .andExpect(status().isForbidden)
    }
}
