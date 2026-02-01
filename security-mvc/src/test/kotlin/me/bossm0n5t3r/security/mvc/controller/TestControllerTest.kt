package me.bossm0n5t3r.security.mvc.controller

import io.mockk.every
import io.mockk.mockk
import me.bossm0n5t3r.security.mvc.annotation.AuthRole
import me.bossm0n5t3r.security.mvc.aop.AuthRoleAspect
import me.bossm0n5t3r.security.mvc.dto.UserDetail
import me.bossm0n5t3r.security.mvc.enumeration.UserRole
import me.bossm0n5t3r.security.mvc.exception.AuthTokenRequiredException
import me.bossm0n5t3r.security.mvc.exception.UserRoleRestrictedException
import me.bossm0n5t3r.security.mvc.service.UserService
import org.aspectj.lang.ProceedingJoinPoint
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

class TestControllerTest {
    private val userService: UserService = mockk()
    private val aspect = AuthRoleAspect()
    private val testController = TestController(userService)

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `publicHello - 직접 호출 시 성공해야 함`() {
        val result = testController.publicHello()
        assertEquals("Hello, Public!", result)
    }

    @Test
    fun `noTokenRequired - AuthRole 검증 통과 후 성공해야 함`() {
        val joinPoint = mockk<ProceedingJoinPoint>()
        val authRole = mockk<AuthRole>()
        every { authRole.requiredToken } returns false
        every { authRole.requiredRoles } returns emptyArray()
        every { joinPoint.proceed() } returns "Hello, No Token Required!"

        val result = aspect.checkAuthRole(joinPoint, authRole)
        assertEquals("Hello, No Token Required!", result)
    }

    @Test
    fun `tokenRequired - 토큰이 없으면 AuthTokenRequiredException 발생해야 함`() {
        val joinPoint = mockk<ProceedingJoinPoint>()
        val authRole = mockk<AuthRole>()
        every { authRole.requiredToken } returns true
        every { authRole.requiredRoles } returns emptyArray()

        assertThrows(AuthTokenRequiredException::class.java) {
            aspect.checkAuthRole(joinPoint, authRole)
        }
    }

    @Test
    fun `tokenRequired - 토큰과 사용자가 있으면 성공해야 함`() {
        val joinPoint = mockk<ProceedingJoinPoint>()
        val authRole = mockk<AuthRole>()
        every { authRole.requiredToken } returns true
        every { authRole.requiredRoles } returns emptyArray()
        every { joinPoint.proceed() } returns "Hello, Token Required!"

        val user =
            UserDetail(userId = "1", username = "user", nickname = "일반사용자", email = "user@example.com", roles = listOf(UserRole.USER))
        val token = "valid-token"
        SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(user, token, user.authorities)

        val result = aspect.checkAuthRole(joinPoint, authRole)
        assertEquals("Hello, Token Required!", result)
    }

    @Test
    fun `adminOnly - ADMIN 권한이 없으면 UserRoleRestrictedException 발생해야 함`() {
        val joinPoint = mockk<ProceedingJoinPoint>()
        val authRole = mockk<AuthRole>()
        every { authRole.requiredToken } returns true
        every { authRole.requiredRoles } returns arrayOf(UserRole.ADMIN)

        val user =
            UserDetail(userId = "1", username = "user", nickname = "일반사용자", email = "user@example.com", roles = listOf(UserRole.USER))
        val token = "valid-token"
        SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(user, token, user.authorities)

        assertThrows(UserRoleRestrictedException::class.java) {
            aspect.checkAuthRole(joinPoint, authRole)
        }
    }

    @Test
    fun `me - UserService를 통해 현재 사용자 정보를 가져와야 함`() {
        val user =
            UserDetail(userId = "1", username = "user", nickname = "일반사용자", email = "user@example.com", roles = listOf(UserRole.USER))
        SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(user, null, user.authorities)

        every { userService.getCurrentUser() } returns user

        val result = testController.me()
        assertEquals(user, result)
    }

    @Test
    fun `userOnly - USER 권한으로 접근 시 성공해야 함`() {
        val joinPoint = mockk<ProceedingJoinPoint>()
        val authRole = mockk<AuthRole>()
        every { authRole.requiredToken } returns true
        every { authRole.requiredRoles } returns arrayOf(UserRole.USER)
        every { joinPoint.proceed() } returns "Hello, User Only!"

        val user =
            UserDetail(userId = "1", username = "user", nickname = "일반사용자", email = "user@example.com", roles = listOf(UserRole.USER))
        val token = "valid-token"
        SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(user, token, user.authorities)

        val result = aspect.checkAuthRole(joinPoint, authRole)
        assertEquals("Hello, User Only!", result)
    }

    @Test
    fun `premiumOnly - PREMIUM 권한으로 접근 시 성공해야 함`() {
        val joinPoint = mockk<ProceedingJoinPoint>()
        val authRole = mockk<AuthRole>()
        every { authRole.requiredToken } returns true
        every { authRole.requiredRoles } returns arrayOf(UserRole.PREMIUM)
        every { joinPoint.proceed() } returns "Hello, Premium Only!"

        val user =
            UserDetail(
                userId = "1",
                username = "premium",
                nickname = "프리미엄",
                email = "premium@example.com",
                roles = listOf(UserRole.PREMIUM),
            )
        val token = "valid-token"
        SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(user, token, user.authorities)

        val result = aspect.checkAuthRole(joinPoint, authRole)
        assertEquals("Hello, Premium Only!", result)
    }

    @Test
    fun `anonymousOnly - ANONYMOUS 권한으로 접근 시 성공해야 함`() {
        val joinPoint = mockk<ProceedingJoinPoint>()
        val authRole = mockk<AuthRole>()
        every { authRole.requiredToken } returns true
        every { authRole.requiredRoles } returns arrayOf(UserRole.ANONYMOUS)
        every { joinPoint.proceed() } returns "Hello, Anonymous Only!"

        val user =
            UserDetail(
                userId = "1",
                username = "anonymous",
                nickname = "익명사용자",
                email = "anonymous@example.com",
                roles = listOf(UserRole.ANONYMOUS),
            )
        val token = "valid-token"
        SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(user, token, user.authorities)

        val result = aspect.checkAuthRole(joinPoint, authRole)
        assertEquals("Hello, Anonymous Only!", result)
    }

    @Test
    fun `requiredRoles가 비어있지 않을 때 토큰이 없으면 AuthTokenRequiredException 발생해야 함`() {
        val joinPoint = mockk<ProceedingJoinPoint>()
        val authRole = mockk<AuthRole>()
        every { authRole.requiredToken } returns false
        every { authRole.requiredRoles } returns arrayOf(UserRole.USER)

        assertThrows(AuthTokenRequiredException::class.java) {
            aspect.checkAuthRole(joinPoint, authRole)
        }
    }
}
