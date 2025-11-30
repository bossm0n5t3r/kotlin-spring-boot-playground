package me.bossm0n5t3r.sse.service

import me.bossm0n5t3r.sse.dto.SseEvent
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface EventStore {
    fun publish(message: String): Mono<SseEvent>

    fun streamFrom(lastStreamId: String?): Flux<SseEvent>
}
