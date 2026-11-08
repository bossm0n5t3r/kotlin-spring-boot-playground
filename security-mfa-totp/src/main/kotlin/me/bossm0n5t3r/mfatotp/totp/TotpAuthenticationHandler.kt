package me.bossm0n5t3r.mfatotp.totp

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import me.bossm0n5t3r.mfatotp.user.TotpUserDetails
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.FactorGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.stereotype.Component

@Component
class TotpAuthenticationHandler : AuthenticationSuccessHandler {
    private val securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy()
    private val securityContextRepository: SecurityContextRepository =
        HttpSessionSecurityContextRepository()

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication,
    ) {
        val userDetails = authentication.principal as TotpUserDetails

        if (userDetails.isMfaEnabled() && userDetails.getTotpSecretOrNull() != null) {
            response.sendRedirect("/totp/verify")
        } else {
            response.sendRedirect("/totp/setup")
        }
    }

    fun grantTotpFactor(
        authentication: Authentication,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        val authorities = authentication.authorities.toMutableList()
        authorities.add(FactorGrantedAuthority.fromFactor(TOTP))

        val newAuth =
            UsernamePasswordAuthenticationToken(
                authentication.principal ?: error("Not found principal"),
                authentication.credentials,
                authorities,
            )
        val context =
            securityContextHolderStrategy.createEmptyContext().apply {
                this.authentication = newAuth
            }
        securityContextHolderStrategy.context = context
        // SecurityContextHolderFilter does not save the SecurityContext, so save it explicitly
        // https://docs.spring.io/spring-security/reference/servlet/authentication/persistence.html#securitycontextholderfilter
        // https://docs.spring.io/spring-security/reference/servlet/authentication/session-management.html#store-authentication-manually
        securityContextRepository.saveContext(context, request, response)
    }

    companion object {
        private const val TOTP = "TOTP"
        const val TOTP_AUTHORITY = "FACTOR_TOTP"
    }
}
