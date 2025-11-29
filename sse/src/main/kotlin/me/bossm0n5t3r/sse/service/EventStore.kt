package me.bossm0n5t3r.sse.service

import me.bossm0n5t3r.sse.dto.SseEvent
import reactor.core.publisher.Flux

interface EventStore {
    fun publish(message: String): SseEvent

    fun streamFrom(lastEventId: String?): Flux<SseEvent>
}
