package me.bossm0n5t3r.account.model

import me.bossm0n5t3r.account.enumeration.UserRole

data class UserAccountResponse(
    val id: Long?,
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
    val role: UserRole? = null,
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
