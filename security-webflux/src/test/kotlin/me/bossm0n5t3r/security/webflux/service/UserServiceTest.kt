package me.bossm0n5t3r.security.webflux.service

import kotlinx.coroutines.runBlocking
import me.bossm0n5t3r.security.webflux.context.ReactiveUserContext
import me.bossm0n5t3r.security.webflux.dto.UserDetail
import me.bossm0n5t3r.security.webflux.enumeration.UserRole
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UserServiceTest {
    private val userService = UserService()

    @Test
    fun `getCurrentUser - should return UserDetail when present in context`() =
        runBlocking {
            val user =
                UserDetail(
                    userId = "testUser",
                    username = "testUser",
                    nickname = "testNickname",
                    email = "test@example.com",
                    roles = listOf(UserRole.USER),
                )
            val token = "testToken"

            val reactorContext =
                reactor.util.context.Context
                    .empty()
                    .let { ReactiveUserContext.putAll(it, user = user, token = token) }

            kotlinx.coroutines.withContext(kotlinx.coroutines.reactor.ReactorContext(reactorContext)) {
                val currentUser = userService.getCurrentUser()
                assertEquals(user, currentUser)
            }
        }

    @Test
    fun `getCurrentUser - should return null when not present in context`() =
        runBlocking {
            val currentUser = userService.getCurrentUser()
            assertEquals(null, currentUser)
        }
}
