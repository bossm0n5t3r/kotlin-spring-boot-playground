package me.bossm0n5t3r.security.mvc.config

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import me.bossm0n5t3r.security.mvc.client.AccountClient
import me.bossm0n5t3r.security.mvc.context.UserContext
import me.bossm0n5t3r.security.mvc.dto.UserDetail
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView

@Component
class AuthInterceptor(
    private val accountClient: AccountClient,
) : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        val startMillis = System.currentTimeMillis()
        UserContext.setStartMillis(startMillis)

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
                UserContext.setUserDetail(userDetail)
                UserContext.setAuthToken(userToken)
            } catch (e: Exception) {
                // Ignore error and continue as anonymous if token is invalid or service is down
                LOGGER.error("Failed to fetch user account", e)
            }
        }
        return true
    }

    override fun postHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        modelAndView: ModelAndView?,
    ) {
        val startMillis = UserContext.getStartMillis() ?: System.currentTimeMillis()
        val elapsedMillis = System.currentTimeMillis() - startMillis

        LOGGER.info(
            "method: {}, path: {}, status: {}, elapsedMillis: {}",
            request.method,
            request.requestURI,
            response.status,
            elapsedMillis,
        )
    }

    override fun afterCompletion(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        ex: Exception?,
    ) {
        UserContext.clear()
    }
}
