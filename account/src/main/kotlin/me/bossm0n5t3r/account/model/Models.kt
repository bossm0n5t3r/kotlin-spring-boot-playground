package me.bossm0n5t3r.account.model

data class UserAccountResponse(
    val id: Long?,
    val username: String,
    val nickname: String,
    val email: String,
)

data class RegisterRequest(
    val username: String,
    val nickname: String,
    val email: String,
    val password: String,
)

data class TokenResponse(
    val token: String,
)

data class LoginRequest(
    val username: String,
    val password: String,
)
