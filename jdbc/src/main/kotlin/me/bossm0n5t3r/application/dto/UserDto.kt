package me.bossm0n5t3r.application.dto

import me.bossm0n5t3r.domain.User

data class UserDto(
    val id: UserId,
    val name: String,
    val age: Int,
) {
    constructor(user: User) : this(
        id = UserId(user.id.value),
        name = user.name,
        age = user.age,
    )
}

fun User.toUserDto() = UserDto(this)
