package me.bossm0n5t3r.security.webflux.aop

import me.bossm0n5t3r.security.webflux.annotation.AuthRole
import me.bossm0n5t3r.security.webflux.context.ReactiveUserContext
import me.bossm0n5t3r.security.webflux.exception.AuthTokenRequiredException
import me.bossm0n5t3r.security.webflux.exception.UserRoleRestrictedException
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Aspect
@Component
class AuthRoleAspect {
    @Around("@annotation(authRole)")
    fun checkAuthRole(
        joinPoint: ProceedingJoinPoint,
        authRole: AuthRole,
    ): Mono<*> {
        return Mono.deferContextual { ctxView ->
            val user = ReactiveUserContext.userFrom(ctxView)
            val token = ReactiveUserContext.tokenFrom(ctxView)

            if (authRole.requiredToken || authRole.requiredRoles.isNotEmpty()) {
                if (token == null || user == null) {
                    return@deferContextual Mono.error(AuthTokenRequiredException())
                }
            }

            if (user != null && authRole.requiredRoles.isNotEmpty()) {
                val hasRequiredRole = authRole.requiredRoles.any { it in user.roles }
                if (!hasRequiredRole) {
                    return@deferContextual Mono.error<Any>(UserRoleRestrictedException())
                }
            }

            when (val result = joinPoint.proceed()) {
                is Mono<*> -> result
                else -> Mono.justOrEmpty(result)
            }
        }
    }
}
