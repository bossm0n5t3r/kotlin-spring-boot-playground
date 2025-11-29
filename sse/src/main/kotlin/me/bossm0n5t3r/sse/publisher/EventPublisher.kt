package me.bossm0n5t3r.sse.publisher

import me.bossm0n5t3r.sse.service.EventStore

interface EventPublisher {
    val eventStore: EventStore

    fun publish()
}
