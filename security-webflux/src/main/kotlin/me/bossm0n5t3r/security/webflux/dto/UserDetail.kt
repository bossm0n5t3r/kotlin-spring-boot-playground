package me.bossm0n5t3r.security.webflux.dto

import me.bossm0n5t3r.security.webflux.enumeration.UserRole

data class UserDetail(
    val userId: String,
    val username: String,
    val nickname: String,
    val email: String,
    val roles: List<UserRole>,
)
