package me.bossm0n5t3r.infrastructure.http

import me.bossm0n5t3r.infrastructure.http.dto.CreateTodoRequest
import me.bossm0n5t3r.infrastructure.http.dto.TodoDto
import me.bossm0n5t3r.infrastructure.http.dto.UpdateTodoRequest
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.service.annotation.DeleteExchange
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.service.annotation.PostExchange
import org.springframework.web.service.annotation.PutExchange

/**
 * Declarative HTTP client interface backed by WebClient via HttpServiceProxyFactory.
 * Mirrors the endpoints used by WebTodoService for /todos resources.
 */
@HttpExchange(url = "/todos")
interface JsonPlaceholderWebClient {
    @GetExchange("/{id}")
    suspend fun getTodo(
        @PathVariable id: Long,
    ): TodoDto

    @PostExchange
    suspend fun createTodo(
        @RequestBody request: CreateTodoRequest,
    ): TodoDto

    @PutExchange("/{id}")
    suspend fun updateTodo(
        @PathVariable id: Long,
        @RequestBody request: UpdateTodoRequest,
    ): TodoDto

    @DeleteExchange("/{id}")
    suspend fun deleteTodo(
        @PathVariable id: Long,
    )
}
