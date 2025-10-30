package me.bossm0n5t3r.application

import me.bossm0n5t3r.infrastructure.http.JsonPlaceholderWebClient
import me.bossm0n5t3r.infrastructure.http.dto.CreateTodoRequest
import me.bossm0n5t3r.infrastructure.http.dto.TodoDto
import me.bossm0n5t3r.infrastructure.http.dto.UpdateTodoRequest
import org.springframework.stereotype.Service

@Service
class WebClientTodoService(
    private val client: JsonPlaceholderWebClient,
) {
    suspend fun getTodo(id: Long): TodoDto = client.getTodo(id)

    suspend fun create(request: CreateTodoRequest): TodoDto = client.createTodo(request)

    suspend fun update(
        id: Long,
        request: UpdateTodoRequest,
    ): TodoDto = client.updateTodo(id, request)

    suspend fun delete(id: Long) = client.deleteTodo(id)
}
