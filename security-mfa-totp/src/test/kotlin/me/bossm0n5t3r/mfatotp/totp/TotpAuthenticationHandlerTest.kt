package me.bossm0n5t3r.mfatotp.totp

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import me.bossm0n5t3r.mfatotp.user.TotpUserDetails
import me.bossm0n5t3r.mfatotp.user.User
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.FactorGrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.context.HttpSessionSecurityContextRepository

class TotpAuthenticationHandlerTest {
    private val sut = TotpAuthenticationHandler()
    private val request = MockHttpServletRequest()
    private val response = MockHttpServletResponse()

    @AfterTest
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun onAuthenticationSuccess_redirects_to_totp_setup_if_mfa_is_not_enabled() {
        // given
        val authentication = passwordAuthenticationOf(User("alice", "password"))

        // when
        sut.onAuthenticationSuccess(request, response, authentication)

        // then
        assertEquals("/totp/setup", response.redirectedUrl)
    }

    @Test
    fun onAuthenticationSuccess_redirects_to_totp_verify_if_mfa_is_enabled() {
        // given
        val user =
            User("alice", "password").apply {
                totpSecret = "ABCDEFGHIJKLMNOP"
                mfaEnabled = true
            }
        val authentication = passwordAuthenticationOf(user)

        // when
        sut.onAuthenticationSuccess(request, response, authentication)

        // then
        assertEquals("/totp/verify", response.redirectedUrl)
    }

    @Test
    fun onAuthenticationSuccess_redirects_to_totp_setup_if_mfa_is_enabled_but_secret_is_missing() {
        // given
        val user = User("alice", "password").apply { mfaEnabled = true }
        val authentication = passwordAuthenticationOf(user)

        // when
        sut.onAuthenticationSuccess(request, response, authentication)

        // then
        assertEquals("/totp/setup", response.redirectedUrl)
    }

    @Test
    fun grantTotpFactor_adds_totp_factor_and_keeps_existing_authorities() {
        // given
        val authentication = passwordAuthenticationOf(User("alice", "password"))

        // when
        sut.grantTotpFactor(authentication, request, response)

        // then
        val newAuthentication = assertNotNull(SecurityContextHolder.getContext().authentication)
        assertSame(authentication.principal, newAuthentication.principal)
        assertEquals(
            setOf(
                ROLE_USER,
                FactorGrantedAuthority.PASSWORD_AUTHORITY,
                TotpAuthenticationHandler.TOTP_AUTHORITY,
            ),
            newAuthentication.authorities.map { it.authority }.toSet(),
        )
    }

    @Test
    fun grantTotpFactor_saves_security_context_to_session() {
        // given
        val authentication = passwordAuthenticationOf(User("alice", "password"))

        // when
        sut.grantTotpFactor(authentication, request, response)

        // then
        val session = assertNotNull(request.getSession(false))
        val savedContext =
            assertIs<SecurityContext>(
                session.getAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                ),
            )
        assertSame(SecurityContextHolder.getContext().authentication, savedContext.authentication)
        assertEquals(
            true,
            savedContext.authentication?.authorities?.any {
                it.authority == TotpAuthenticationHandler.TOTP_AUTHORITY
            },
        )
    }

    private fun passwordAuthenticationOf(user: User): Authentication {
        val userDetails = TotpUserDetails(user)
        return UsernamePasswordAuthenticationToken.authenticated(
            userDetails,
            null,
            userDetails.authorities + FactorGrantedAuthority.fromFactor("PASSWORD"),
        )
    }

    companion object {
        private const val ROLE_USER = "ROLE_USER"
    }
}
