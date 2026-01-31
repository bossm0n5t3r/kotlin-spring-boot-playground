package me.bossm0n5t3r.security.mvc.dto

import me.bossm0n5t3r.security.mvc.enumeration.UserRole

data class UserAccountResponse(
    val id: Long,
    val username: String,
    val nickname: String,
    val email: String,
    val role: UserRole,
)

data class RegisterRequest(
    val username: String,
    val nickname: String,
    val email: String,
    val password: String,
)

data class UpdateRoleRequest(
    val role: UserRole,
)

data class TokenResponse(
    val token: String,
)

data class LoginRequest(
    val username: String,
    val password: String,
)
