package me.bossm0n5t3r.txroutingdatasource.presentation.dto

import me.bossm0n5t3r.txroutingdatasource.domain.entity.User

data class UserResponse(
    val id: Long,
    val name: String,
    val email: String,
)

fun User.toUserResponse(): UserResponse =
    UserResponse(
        id = requireNotNull(id),
        name = name,
        email = email,
    )