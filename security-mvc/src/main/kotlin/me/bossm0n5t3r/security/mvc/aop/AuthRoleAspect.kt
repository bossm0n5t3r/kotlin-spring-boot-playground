package me.bossm0n5t3r.security.mvc.aop

import me.bossm0n5t3r.security.mvc.annotation.AuthRole
import me.bossm0n5t3r.security.mvc.context.UserContext
import me.bossm0n5t3r.security.mvc.exception.AuthTokenRequiredException
import me.bossm0n5t3r.security.mvc.exception.UserRoleRestrictedException
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.stereotype.Component

@Aspect
@Component
class AuthRoleAspect {
    @Around("@annotation(authRole)")
    fun checkAuthRole(
        joinPoint: ProceedingJoinPoint,
        authRole: AuthRole,
    ): Any? {
        val user = UserContext.getUserDetail()
        val token = UserContext.getAuthToken()

        if (authRole.requiredToken || authRole.requiredRoles.isNotEmpty()) {
            if (token == null || user == null) {
                throw AuthTokenRequiredException()
            }
        }

        if (user != null && authRole.requiredRoles.isNotEmpty()) {
            val hasRequiredRole = authRole.requiredRoles.any { it in user.roles }
            if (!hasRequiredRole) {
                throw UserRoleRestrictedException()
            }
        }

        return joinPoint.proceed()
    }
}
