package me.bossm0n5t3r.infrastructure.http

import org.springframework.web.service.annotation.HttpExchange

/**
 * Declarative HTTP client interface backed by WebClient via HttpServiceProxyFactory.
 * Mirrors the endpoints used by WebTodoService for /todos resources.
 */
@HttpExchange(url = "/todos")
interface JsonPlaceholderWebClient : JsonPlaceholderClient
