package me.bossm0n5t3r.infrastructure

import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.annotation.HttpExchange

@HttpExchange
interface JsonPlaceHolderRestClient {
    @GetExchange("/posts")
    fun getPosts(): String

    @GetExchange("/users")
    fun getUsers(): String

    @GetExchange("/todos")
    fun getTodos(): String
}
