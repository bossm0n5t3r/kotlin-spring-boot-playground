package me.bossm0n5t3r.sse.controller

import me.bossm0n5t3r.sse.dto.SseEvent
import me.bossm0n5t3r.sse.service.EventStore
import org.springframework.http.codec.ServerSentEvent
import reactor.core.publisher.Flux

interface SseController {
    val eventStore: EventStore

    fun sse(lastEventId: String?): Flux<ServerSentEvent<SseEvent>>
}
