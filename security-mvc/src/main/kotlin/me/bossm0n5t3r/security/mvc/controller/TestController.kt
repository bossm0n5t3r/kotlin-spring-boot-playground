package me.bossm0n5t3r.security.mvc.controller

import me.bossm0n5t3r.security.mvc.annotation.AuthRole
import me.bossm0n5t3r.security.mvc.dto.UserDetail
import me.bossm0n5t3r.security.mvc.enumeration.UserRole
import me.bossm0n5t3r.security.mvc.service.UserService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController(
    private val userService: UserService,
) {
    @GetMapping("/public/hello")
    fun publicHello(): String = "Hello, Public!"

    @AuthRole(requiredToken = true)
    @GetMapping("/test/me")
    fun me(): UserDetail? = userService.getCurrentUser()

    @AuthRole(requiredToken = false)
    @GetMapping("/test/no-token-required")
    fun noTokenRequired(): String = "Hello, No Token Required!"

    @AuthRole(requiredToken = true)
    @GetMapping("/test/token-required")
    fun tokenRequired(): String = "Hello, Token Required!"

    @AuthRole(requiredRoles = [UserRole.ADMIN])
    @GetMapping("/test/admin-only")
    fun adminOnly(): String = "Hello, Admin Only!"

    @AuthRole(requiredRoles = [UserRole.USER])
    @GetMapping("/test/user-only")
    fun userOnly(): String = "Hello, User Only!"

    @AuthRole(requiredRoles = [UserRole.PREMIUM])
    @GetMapping("/test/premium-only")
    fun premiumOnly(): String = "Hello, Premium Only!"

    @AuthRole(requiredRoles = [UserRole.ANONYMOUS])
    @GetMapping("/test/anonymous-only")
    fun anonymousOnly(): String = "Hello, Anonymous Only!"
}
