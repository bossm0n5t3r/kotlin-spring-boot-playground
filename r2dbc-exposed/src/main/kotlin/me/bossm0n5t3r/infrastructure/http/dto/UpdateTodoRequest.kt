package me.bossm0n5t3r.infrastructure.http.dto

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Request body for updating a Todo in jsonplaceholder.
 * We allow updating title and completed fields.
 */
data class UpdateTodoRequest(
    @field:JsonProperty("title")
    val title: String,
    @field:JsonProperty("completed")
    val completed: Boolean,
)
