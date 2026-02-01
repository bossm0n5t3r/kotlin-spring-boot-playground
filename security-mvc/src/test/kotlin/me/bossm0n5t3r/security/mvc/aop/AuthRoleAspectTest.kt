package me.bossm0n5t3r.security.mvc.aop

import io.mockk.every
import io.mockk.mockk
import me.bossm0n5t3r.security.mvc.annotation.AuthRole
import me.bossm0n5t3r.security.mvc.context.UserContext
import me.bossm0n5t3r.security.mvc.dto.UserDetail
import me.bossm0n5t3r.security.mvc.enumeration.ResponseStatus
import me.bossm0n5t3r.security.mvc.enumeration.UserRole
import me.bossm0n5t3r.security.mvc.exception.AuthTokenRequiredException
import me.bossm0n5t3r.security.mvc.exception.UserRoleRestrictedException
import org.aspectj.lang.ProceedingJoinPoint
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class AuthRoleAspectTest {
    private val aspect = AuthRoleAspect()
    private val joinPoint: ProceedingJoinPoint = mockk()

    @AfterEach
    fun tearDown() {
        UserContext.clear()
    }

    @Test
    fun `requiredToken=true 인데 Context에 token이나 user 없으면 AUTH_TOKEN_REQUIRED 발생`() {
        val authRole = mockk<AuthRole>()
        every { authRole.requiredToken } returns true
        every { authRole.requiredRoles } returns emptyArray()

        val exception =
            assertThrows(AuthTokenRequiredException::class.java) {
                aspect.checkAuthRole(joinPoint, authRole)
            }
        assertEquals(ResponseStatus.AUTH_TOKEN_REQUIRED, exception.status)
    }

    @Test
    fun `user가 필요한 권한을 가지고 있지 않으면 USER_ROLE_RESTRICTED 발생`() {
        val authRole = mockk<AuthRole>()
        every { authRole.requiredToken } returns true
        every { authRole.requiredRoles } returns arrayOf(UserRole.ADMIN)

        val user =
            UserDetail(
                userId = "user1",
                username = "user1",
                nickname = "nickname1",
                email = "user1@example.com",
                roles = listOf(UserRole.USER, UserRole.PREMIUM),
            )
        val token = "token1"
        UserContext.setUserDetail(user)
        UserContext.setAuthToken(token)

        val exception =
            assertThrows(UserRoleRestrictedException::class.java) {
                aspect.checkAuthRole(joinPoint, authRole)
            }
        assertEquals(ResponseStatus.USER_ROLE_RESTRICTED, exception.status)
    }

    @Test
    fun `user가 PREMIUM 권한만 있어도 ADMIN 전용 API는 접근 불가`() {
        val authRole = mockk<AuthRole>()
        every { authRole.requiredToken } returns true
        every { authRole.requiredRoles } returns arrayOf(UserRole.ADMIN)

        val user =
            UserDetail(
                userId = "premiumUser",
                username = "premiumUser",
                nickname = "premiumNickname",
                email = "premium@example.com",
                roles = listOf(UserRole.PREMIUM),
            )
        val token = "token1"
        UserContext.setUserDetail(user)
        UserContext.setAuthToken(token)

        val exception =
            assertThrows(UserRoleRestrictedException::class.java) {
                aspect.checkAuthRole(joinPoint, authRole)
            }
        assertEquals(ResponseStatus.USER_ROLE_RESTRICTED, exception.status)
    }

    @Test
    fun `user가 PREMIUM 권한이 있으면 PREMIUM 전용 API 접근 가능`() {
        val authRole = mockk<AuthRole>()
        every { authRole.requiredToken } returns true
        every { authRole.requiredRoles } returns arrayOf(UserRole.PREMIUM)

        val user =
            UserDetail(
                userId = "premiumUser",
                username = "premiumUser",
                nickname = "premiumNickname",
                email = "premium@example.com",
                roles = listOf(UserRole.PREMIUM),
            )
        val token = "token1"
        UserContext.setUserDetail(user)
        UserContext.setAuthToken(token)

        every { joinPoint.proceed() } returns "Premium Success"

        val result = aspect.checkAuthRole(joinPoint, authRole)
        assertEquals("Premium Success", result)
    }

    @Test
    fun `ANONYMOUS 권한 테스트`() {
        val authRole = mockk<AuthRole>()
        every { authRole.requiredToken } returns true
        every { authRole.requiredRoles } returns arrayOf(UserRole.ANONYMOUS)

        val user =
            UserDetail(
                userId = "anonymousUser",
                username = "anonymousUser",
                nickname = "anonymousNickname",
                email = "anonymous@example.com",
                roles = listOf(UserRole.ANONYMOUS),
            )
        val token = "token1"
        UserContext.setUserDetail(user)
        UserContext.setAuthToken(token)

        every { joinPoint.proceed() } returns "Anonymous Success"

        val result = aspect.checkAuthRole(joinPoint, authRole)
        assertEquals("Anonymous Success", result)
    }

    @Test
    fun `조건 충족 시 정상적으로 proceed 호출`() {
        val authRole = mockk<AuthRole>()
        every { authRole.requiredToken } returns true
        every { authRole.requiredRoles } returns arrayOf(UserRole.USER)

        val user =
            UserDetail(
                userId = "user1",
                username = "user1",
                nickname = "nickname1",
                email = "user1@example.com",
                roles = listOf(UserRole.USER),
            )
        val token = "token1"
        UserContext.setUserDetail(user)
        UserContext.setAuthToken(token)

        every { joinPoint.proceed() } returns "Success"

        val result = aspect.checkAuthRole(joinPoint, authRole)
        assertEquals("Success", result)
    }

    @Test
    fun `proceed 결과가 직접 반환되어 정상 동작`() {
        val authRole = mockk<AuthRole>()
        every { authRole.requiredToken } returns false
        every { authRole.requiredRoles } returns emptyArray()

        every { joinPoint.proceed() } returns "Direct Result"

        val result = aspect.checkAuthRole(joinPoint, authRole)
        assertEquals("Direct Result", result)
    }
}
