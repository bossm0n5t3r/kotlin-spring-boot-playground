package me.bossm0n5t3r.presentation.dto

import me.bossm0n5t3r.application.dto.UserDto

data class UserResponse(
    val id: Long,
    val name: String,
    val age: Int,
) {
    constructor(userDto: UserDto) : this(
        id = userDto.id.value,
        name = userDto.name,
        age = userDto.age,
    )
}

fun UserDto.toUserResponse() = UserResponse(this)
