package me.bossm0n5t3r.security.mvc.service

import me.bossm0n5t3r.security.mvc.context.UserContext
import me.bossm0n5t3r.security.mvc.dto.UserDetail
import me.bossm0n5t3r.security.mvc.enumeration.UserRole
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UserServiceTest {
    private val userService = UserService()

    @AfterEach
    fun tearDown() {
        UserContext.clear()
    }

    @Test
    fun `getCurrentUser - should return UserDetail when present in context`() {
        val user =
            UserDetail(
                userId = "testUser",
                username = "testUser",
                nickname = "testNickname",
                email = "test@example.com",
                roles = listOf(UserRole.USER),
            )
        UserContext.setUserDetail(user)

        val currentUser = userService.getCurrentUser()
        assertEquals(user, currentUser)
    }

    @Test
    fun `getCurrentUser - should return null when not present in context`() {
        val currentUser = userService.getCurrentUser()
        assertEquals(null, currentUser)
    }
}
