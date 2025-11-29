package me.bossm0n5t3r.sse.controller

import me.bossm0n5t3r.sse.dto.SseEvent
import me.bossm0n5t3r.sse.service.EventStore
import org.springframework.http.MediaType
import org.springframework.http.codec.ServerSentEvent
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import java.time.Duration

@RestController
@RequestMapping("/sse")
class SseController(
    private val eventStore: EventStore,
) {
    companion object {
        private const val LAST_EVENT_ID = "Last-Event-ID"
    }

    @GetMapping(produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun sse(
        @RequestHeader(LAST_EVENT_ID, required = false)
        lastEventId: String?,
    ): Flux<ServerSentEvent<SseEvent>> =
        eventStore
            .streamFrom(lastEventId)
            .map { event ->
                ServerSentEvent
                    .builder<SseEvent>()
                    .id(event.streamId)
                    .event("message")
                    .data(event)
                    .retry(Duration.ofSeconds(3)) // 끊기면 3초 후 재접속
                    .build()
            }
}
