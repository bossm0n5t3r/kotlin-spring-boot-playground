package me.bossm0n5t3r.presentation

import me.bossm0n5t3r.infrastructure.JsonPlaceHolderRestClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/http-exchange")
class JsonPlaceHolderController(
    private val jsonPlaceHolderRestClient: JsonPlaceHolderRestClient,
) {
    @GetMapping("/posts")
    fun getPosts() = jsonPlaceHolderRestClient.getPosts()

    @GetMapping("/users")
    fun getUsers() = jsonPlaceHolderRestClient.getUsers()

    @GetMapping("/todos")
    fun getTodos() = jsonPlaceHolderRestClient.getTodos()
}
