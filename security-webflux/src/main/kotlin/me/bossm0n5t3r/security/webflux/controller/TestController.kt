package me.bossm0n5t3r.security.webflux.controller

import me.bossm0n5t3r.security.webflux.annotation.AuthRole
import me.bossm0n5t3r.security.webflux.dto.UserDetail
import me.bossm0n5t3r.security.webflux.enumeration.UserRole
import me.bossm0n5t3r.security.webflux.service.UserService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController(
    private val userService: UserService,
) {
    @GetMapping("/public/hello")
    suspend fun publicHello(): String = "Hello, Public!"

    @AuthRole(requiredToken = true)
    @GetMapping("/test/me")
    suspend fun me(): UserDetail? = userService.getCurrentUser()

    @AuthRole(requiredToken = false)
    @GetMapping("/test/no-token-required")
    suspend fun noTokenRequired(): String = "Hello, No Token Required!"

    @AuthRole(requiredToken = true)
    @GetMapping("/test/token-required")
    suspend fun tokenRequired(): String = "Hello, Token Required!"

    @AuthRole(requiredRoles = [UserRole.ADMIN])
    @GetMapping("/test/admin-only")
    suspend fun adminOnly(): String = "Hello, Admin Only!"

    @AuthRole(requiredRoles = [UserRole.USER])
    @GetMapping("/test/user-only")
    suspend fun userOnly(): String = "Hello, User Only!"

    @AuthRole(requiredRoles = [UserRole.PREMIUM])
    @GetMapping("/test/premium-only")
    suspend fun premiumOnly(): String = "Hello, Premium Only!"

    @AuthRole(requiredRoles = [UserRole.ANONYMOUS])
    @GetMapping("/test/anonymous-only")
    suspend fun anonymousOnly(): String = "Hello, Anonymous Only!"
}
