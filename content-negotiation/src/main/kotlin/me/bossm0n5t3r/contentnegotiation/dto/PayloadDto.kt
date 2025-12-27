package me.bossm0n5t3r.contentnegotiation.dto

data class PayloadDto(
    val id: String,
    val content: String,
    val data: List<String> = emptyList(),
)
