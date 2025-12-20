package me.bossm0n5t3r.dto

import kotlinx.serialization.Serializable

@Serializable
data class PersonDto(
    val name: String,
    val age: Int,
    val email: String? = null,
    val hobbies: List<String> = emptyList(),
)
