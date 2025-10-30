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
 * Declarative HTTP client interface backed by RestClient via HttpServiceProxyFactory.
 * Mirrors the endpoints used by RestTodoService for /todos resources.
 */
@HttpExchange(url = "/todos")
interface JsonPlaceholderRestClient {
    @GetExchange("/{id}")
    fun getTodo(
        @PathVariable id: Long,
    ): TodoDto

    @PostExchange
    fun createTodo(
        @RequestBody request: CreateTodoRequest,
    ): TodoDto

    @PutExchange("/{id}")
    fun updateTodo(
        @PathVariable id: Long,
        @RequestBody request: UpdateTodoRequest,
    ): TodoDto

    @DeleteExchange("/{id}")
    fun deleteTodo(
        @PathVariable id: Long,
    )
}
