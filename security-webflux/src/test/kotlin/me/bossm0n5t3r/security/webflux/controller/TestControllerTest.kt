package me.bossm0n5t3r.security.webflux.controller

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.reactor.ReactorContext
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import me.bossm0n5t3r.security.webflux.annotation.AuthRole
import me.bossm0n5t3r.security.webflux.aop.AuthRoleAspect
import me.bossm0n5t3r.security.webflux.context.ReactiveUserContext
import me.bossm0n5t3r.security.webflux.dto.UserDetail
import me.bossm0n5t3r.security.webflux.enumeration.UserRole
import me.bossm0n5t3r.security.webflux.service.UserService
import org.aspectj.lang.ProceedingJoinPoint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import reactor.util.context.Context

class TestControllerTest {
    private val userService: UserService = mockk()
    private val aspect = AuthRoleAspect()
    private val testController = TestController(userService)

    @Test
    fun `publicHello - 직접 호출 시 성공해야 함`() =
        runTest {
            val result = testController.publicHello()
            assertEquals("Hello, Public!", result)
        }

    @Test
    fun `noTokenRequired - AuthRole 검증 통과 후 성공해야 함`() {
        val joinPoint = mockk<ProceedingJoinPoint>()
        val authRole = mockk<AuthRole>()
        every { authRole.requiredToken } returns false
        every { authRole.requiredRoles } returns emptyArray()

        // joinPoint.proceed()가 호출될 때 실제 컨트롤러 메서드가 실행되도록 설정하거나 모킹
        every { joinPoint.proceed() } returns Mono.just("Hello, No Token Required!")

        val result = aspect.checkAuthRole(joinPoint, authRole)

        StepVerifier
            .create(result)
            .expectNext("Hello, No Token Required!")
            .verifyComplete()
    }

    @Test
    fun `tokenRequired - 토큰이 없으면 AuthTokenRequiredException 발생해야 함`() {
        val joinPoint = mockk<ProceedingJoinPoint>()
        val authRole = mockk<AuthRole>()
        every { authRole.requiredToken } returns true
        every { authRole.requiredRoles } returns emptyArray()

        val result = aspect.checkAuthRole(joinPoint, authRole)

        StepVerifier
            .create(result)
            .expectErrorMatches { it is me.bossm0n5t3r.security.webflux.exception.AuthTokenRequiredException }
            .verify()
    }

    @Test
    fun `tokenRequired - 토큰과 사용자가 있으면 성공해야 함`() {
        val joinPoint = mockk<ProceedingJoinPoint>()
        val authRole = mockk<AuthRole>()
        every { authRole.requiredToken } returns true
        every { authRole.requiredRoles } returns emptyArray()
        every { joinPoint.proceed() } returns Mono.just("Hello, Token Required!")

        val user = UserDetail(userId = "1", roles = listOf(UserRole.USER))
        val token = "valid-token"

        val result =
            aspect
                .checkAuthRole(joinPoint, authRole)
                .contextWrite { ctx -> ReactiveUserContext.putAll(ctx, user = user, token = token) }

        StepVerifier
            .create(result)
            .expectNext("Hello, Token Required!")
            .verifyComplete()
    }

    @Test
    fun `adminOnly - ADMIN 권한이 없으면 UserRoleRestrictedException 발생해야 함`() {
        val joinPoint = mockk<ProceedingJoinPoint>()
        val authRole = mockk<AuthRole>()
        every { authRole.requiredToken } returns true
        every { authRole.requiredRoles } returns arrayOf(UserRole.ADMIN)

        val user = UserDetail(userId = "1", roles = listOf(UserRole.USER))
        val token = "valid-token"

        val result =
            aspect
                .checkAuthRole(joinPoint, authRole)
                .contextWrite { ctx -> ReactiveUserContext.putAll(ctx, user = user, token = token) }

        StepVerifier
            .create(result)
            .expectErrorMatches { it is me.bossm0n5t3r.security.webflux.exception.UserRoleRestrictedException }
            .verify()
    }

    @Test
    fun `me - UserService를 통해 현재 사용자 정보를 가져와야 함`() =
        runTest {
            val user = UserDetail(userId = "1", roles = listOf(UserRole.USER))
            val token = "valid-token"

            coEvery { userService.getCurrentUser() } returns user
            val reactorContext = Context.empty().let { ReactiveUserContext.putAll(it, user = user, token = token) }

            val result = withContext(ReactorContext(reactorContext)) { testController.me() }

            assertEquals(user, result)
        }

    @Test
    fun `userOnly - USER 권한으로 접근 시 성공해야 함`() {
        val joinPoint = mockk<ProceedingJoinPoint>()
        val authRole = mockk<AuthRole>()
        every { authRole.requiredToken } returns true
        every { authRole.requiredRoles } returns arrayOf(UserRole.USER)
        every { joinPoint.proceed() } returns Mono.just("Hello, User Only!")

        val user = UserDetail(userId = "1", roles = listOf(UserRole.USER))
        val token = "valid-token"

        val result =
            aspect
                .checkAuthRole(joinPoint, authRole)
                .contextWrite { ctx -> ReactiveUserContext.putAll(ctx, user = user, token = token) }

        StepVerifier
            .create(result)
            .expectNext("Hello, User Only!")
            .verifyComplete()
    }

    @Test
    fun `premiumOnly - PREMIUM 권한으로 접근 시 성공해야 함`() {
        val joinPoint = mockk<ProceedingJoinPoint>()
        val authRole = mockk<AuthRole>()
        every { authRole.requiredToken } returns true
        every { authRole.requiredRoles } returns arrayOf(UserRole.PREMIUM)
        every { joinPoint.proceed() } returns Mono.just("Hello, Premium Only!")

        val user = UserDetail(userId = "1", roles = listOf(UserRole.PREMIUM))
        val token = "valid-token"

        val result =
            aspect
                .checkAuthRole(joinPoint, authRole)
                .contextWrite { ctx -> ReactiveUserContext.putAll(ctx, user = user, token = token) }

        StepVerifier
            .create(result)
            .expectNext("Hello, Premium Only!")
            .verifyComplete()
    }

    @Test
    fun `anonymousOnly - ANONYMOUS 권한으로 접근 시 성공해야 함`() {
        val joinPoint = mockk<ProceedingJoinPoint>()
        val authRole = mockk<AuthRole>()
        every { authRole.requiredToken } returns true
        every { authRole.requiredRoles } returns arrayOf(UserRole.ANONYMOUS)
        every { joinPoint.proceed() } returns Mono.just("Hello, Anonymous Only!")

        val user = UserDetail(userId = "1", roles = listOf(UserRole.ANONYMOUS))
        val token = "valid-token"

        val result =
            aspect
                .checkAuthRole(joinPoint, authRole)
                .contextWrite { ctx -> ReactiveUserContext.putAll(ctx, user = user, token = token) }

        StepVerifier
            .create(result)
            .expectNext("Hello, Anonymous Only!")
            .verifyComplete()
    }
}
