package me.bossm0n5t3r.infrastructure.http.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Simple DTO representing a Todo item from jsonplaceholder.typicode.com
 */
data class TodoDto(
    @field:JsonProperty("userId")
    val userId: Long,
    @field:JsonProperty("id")
    val id: Long,
    @field:JsonProperty("title")
    val title: String,
    @field:JsonProperty("completed")
    val completed: Boolean,
)
