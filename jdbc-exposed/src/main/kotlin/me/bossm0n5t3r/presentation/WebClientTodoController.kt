package me.bossm0n5t3r.presentation

import me.bossm0n5t3r.application.WebClientTodoService
import me.bossm0n5t3r.infrastructure.http.dto.CreateTodoRequest
import me.bossm0n5t3r.infrastructure.http.dto.UpdateTodoRequest
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/web-client")
class WebClientTodoController(
    private val webClientTodoService: WebClientTodoService,
) {
    @GetMapping("/todos/{id}")
    suspend fun getTodo(
        @PathVariable id: Long,
    ) = webClientTodoService.getTodo(id)

    @PostMapping("/todos")
    suspend fun createTodo(
        @RequestBody request: CreateTodoRequest,
    ) = webClientTodoService.create(request)

    @PutMapping("/todos/{id}")
    suspend fun updateTodo(
        @PathVariable id: Long,
        @RequestBody request: UpdateTodoRequest,
    ) = webClientTodoService.update(id, request)

    @DeleteMapping("/todos/{id}")
    suspend fun deleteTodo(
        @PathVariable id: Long,
    ) = webClientTodoService.delete(id)
}
