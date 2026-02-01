package me.bossm0n5t3r.security.mvc.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import me.bossm0n5t3r.security.mvc.client.AccountClient
import me.bossm0n5t3r.security.mvc.dto.UserDetail
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class AuthFilter(
    private val accountClient: AccountClient,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val startMillis = System.currentTimeMillis()

        val userToken = request.getHeader(HttpHeaders.AUTHORIZATION)
        if (userToken != null) {
            try {
                val userAccount = accountClient.getMe(userToken)
                val userDetail =
                    UserDetail(
                        userId = userAccount.id.toString(),
                        username = userAccount.username,
                        nickname = userAccount.nickname,
                        email = userAccount.email,
                        roles = listOf(userAccount.role),
                    )
                val authentication = UsernamePasswordAuthenticationToken(userDetail, userToken, userDetail.authorities)
                SecurityContextHolder.getContext().authentication = authentication
            } catch (e: Exception) {
                // Ignore error and continue as anonymous if token is invalid or service is down
                LOGGER.error("Failed to fetch user account", e)
            }
        }

        try {
            filterChain.doFilter(request, response)
        } finally {
            val elapsedMillis = System.currentTimeMillis() - startMillis
            LOGGER.info(
                "method: {}, path: {}, status: {}, elapsedMillis: {}",
                request.method,
                request.requestURI,
                response.status,
                elapsedMillis,
            )
            SecurityContextHolder.clearContext()
        }
    }
}
