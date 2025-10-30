package me.bossm0n5t3r.infrastructure.http.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Request body for creating a Todo in jsonplaceholder.
 */
data class CreateTodoRequest(
    @field:JsonProperty("userId")
    val userId: Long,
    @field:JsonProperty("title")
    val title: String,
    @field:JsonProperty("completed")
    val completed: Boolean,
)
