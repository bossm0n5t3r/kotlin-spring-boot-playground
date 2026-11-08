package me.bossm0n5t3r.mfatotp.user.dto

import me.bossm0n5t3r.mfatotp.user.User

data class UserDto(
    val username: String,
    val mfaEnabled: Boolean,
)

fun User.toDto(): UserDto = UserDto(username = username, mfaEnabled = mfaEnabled)
