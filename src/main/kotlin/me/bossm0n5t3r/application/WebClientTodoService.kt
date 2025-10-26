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
    fun getTodo(id: Long): TodoDto = client.getTodo(id)

    fun create(request: CreateTodoRequest): TodoDto = client.createTodo(request)

    fun update(
        id: Long,
        request: UpdateTodoRequest,
    ): TodoDto = client.updateTodo(id, request)

    fun delete(id: Long) = client.deleteTodo(id)
}
