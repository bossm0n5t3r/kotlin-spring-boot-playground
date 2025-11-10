package me.bossm0n5t3r.presentation

import me.bossm0n5t3r.application.RestClientTodoService
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
@RequestMapping("/rest-client")
class RestClientTodoController(
    private val restClientTodoService: RestClientTodoService,
) {
    @GetMapping("/todos/{id}")
    fun getTodo(
        @PathVariable id: Long,
    ) = restClientTodoService.getTodo(id)

    @PostMapping("/todos")
    fun createTodo(
        @RequestBody request: CreateTodoRequest,
    ) = restClientTodoService.create(request)

    @PutMapping("/todos/{id}")
    fun updateTodo(
        @PathVariable id: Long,
        @RequestBody request: UpdateTodoRequest,
    ) = restClientTodoService.update(id, request)

    @DeleteMapping("/todos/{id}")
    fun deleteTodo(
        @PathVariable id: Long,
    ) = restClientTodoService.delete(id)
}
