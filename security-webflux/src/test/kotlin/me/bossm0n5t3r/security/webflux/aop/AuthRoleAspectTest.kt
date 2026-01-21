package me.bossm0n5t3r.security.webflux.aop

import io.mockk.every
import io.mockk.mockk
import me.bossm0n5t3r.security.webflux.annotation.AuthRole
import me.bossm0n5t3r.security.webflux.context.ReactiveUserContext
import me.bossm0n5t3r.security.webflux.dto.UserDetail
import me.bossm0n5t3r.security.webflux.enumeration.ResponseStatus
import me.bossm0n5t3r.security.webflux.enumeration.UserRole
import me.bossm0n5t3r.security.webflux.exception.AuthTokenRequiredException
import me.bossm0n5t3r.security.webflux.exception.UserRoleRestrictedException
import org.aspectj.lang.ProceedingJoinPoint
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class AuthRoleAspectTest {
    private val aspect = AuthRoleAspect()
    private val joinPoint: ProceedingJoinPoint = mockk()

    @Test
    fun `requiredToken=true 인데 Context에 token이나 user 없으면 AUTH_TOKEN_REQUIRED 발생`() {
        val authRole = mockk<AuthRole>()
        every { authRole.requiredToken } returns true

        val result = aspect.checkAuthRole(joinPoint, authRole) as Mono<*>

        StepVerifier
            .create(result)
            .expectErrorMatches { it is AuthTokenRequiredException && it.status == ResponseStatus.AUTH_TOKEN_REQUIRED }
            .verify()
    }

    @Test
    fun `user가 필요한 권한을 가지고 있지 않으면 USER_ROLE_RESTRICTED 발생`() {
        val authRole = mockk<AuthRole>()
        every { authRole.requiredToken } returns true
        every { authRole.requiredRoles } returns arrayOf(UserRole.ADMIN)

        val user = UserDetail(userId = "user1", roles = listOf(UserRole.USER, UserRole.PREMIUM))
        val token = "token1"

        val result =
            (aspect.checkAuthRole(joinPoint, authRole) as Mono<*>)
                .contextWrite { ctx -> ReactiveUserContext.putAll(ctx, user = user, token = token) }

        StepVerifier
            .create(result)
            .expectErrorMatches { it is UserRoleRestrictedException && it.status == ResponseStatus.USER_ROLE_RESTRICTED }
            .verify()
    }

    @Test
    fun `user가 PREMIUM 권한만 있어도 ADMIN 전용 API는 접근 불가`() {
        val authRole = mockk<AuthRole>()
        every { authRole.requiredToken } returns true
        every { authRole.requiredRoles } returns arrayOf(UserRole.ADMIN)

        val user = UserDetail(userId = "premiumUser", roles = listOf(UserRole.PREMIUM))
        val token = "token1"

        val result =
            (aspect.checkAuthRole(joinPoint, authRole) as Mono<*>)
                .contextWrite { ctx -> ReactiveUserContext.putAll(ctx, user = user, token = token) }

        StepVerifier
            .create(result)
            .expectErrorMatches { it is UserRoleRestrictedException && it.status == ResponseStatus.USER_ROLE_RESTRICTED }
            .verify()
    }

    @Test
    fun `user가 PREMIUM 권한이 있으면 PREMIUM 전용 API 접근 가능`() {
        val authRole = mockk<AuthRole>()
        every { authRole.requiredToken } returns true
        every { authRole.requiredRoles } returns arrayOf(UserRole.PREMIUM)

        val user = UserDetail(userId = "premiumUser", roles = listOf(UserRole.PREMIUM))
        val token = "token1"
        every { joinPoint.proceed() } returns Mono.just("Premium Success")

        val result =
            (aspect.checkAuthRole(joinPoint, authRole) as Mono<*>)
                .contextWrite { ctx -> ReactiveUserContext.putAll(ctx, user = user, token = token) }

        StepVerifier
            .create(result)
            .expectNext("Premium Success")
            .verifyComplete()
    }

    @Test
    fun `ANONYMOUS 권한 테스트`() {
        val authRole = mockk<AuthRole>()
        every { authRole.requiredToken } returns true
        every { authRole.requiredRoles } returns arrayOf(UserRole.ANONYMOUS)

        val user = UserDetail(userId = "anonymousUser", roles = listOf(UserRole.ANONYMOUS))
        val token = "token1"
        every { joinPoint.proceed() } returns Mono.just("Anonymous Success")

        val result =
            (aspect.checkAuthRole(joinPoint, authRole) as Mono<*>)
                .contextWrite { ctx -> ReactiveUserContext.putAll(ctx, user = user, token = token) }

        StepVerifier
            .create(result)
            .expectNext("Anonymous Success")
            .verifyComplete()
    }

    @Test
    fun `조건 충족 시 정상적으로 proceed 호출`() {
        val authRole = mockk<AuthRole>()
        every { authRole.requiredToken } returns true
        every { authRole.requiredRoles } returns arrayOf(UserRole.USER)

        val user = UserDetail(userId = "user1", roles = listOf(UserRole.USER))
        val token = "token1"
        every { joinPoint.proceed() } returns Mono.just("Success")

        val result =
            (aspect.checkAuthRole(joinPoint, authRole) as Mono<*>)
                .contextWrite { ctx -> ReactiveUserContext.putAll(ctx, user = user, token = token) }

        StepVerifier
            .create(result)
            .expectNext("Success")
            .verifyComplete()
    }

    @Test
    fun `proceed 결과가 Mono가 아닌 경우에도 Mono로 래핑되어 정상 동작`() {
        val authRole = mockk<AuthRole>()
        every { authRole.requiredToken } returns false
        every { authRole.requiredRoles } returns emptyArray()

        every { joinPoint.proceed() } returns "Direct Result"

        val result = (aspect.checkAuthRole(joinPoint, authRole) as Mono<*>)

        StepVerifier
            .create(result)
            .expectNext("Direct Result")
            .verifyComplete()
    }
}
