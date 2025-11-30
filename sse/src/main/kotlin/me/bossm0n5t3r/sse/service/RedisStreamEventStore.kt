package me.bossm0n5t3r.sse.service

import me.bossm0n5t3r.sse.configuration.LOGGER
import me.bossm0n5t3r.sse.dto.SseEvent
import org.springframework.data.domain.Range
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.connection.stream.RecordId
import org.springframework.data.redis.connection.stream.StreamRecords
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import ulid.ULID
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
@Component
class RedisStreamEventStore(
    private val redisTemplate: StringRedisTemplate,
) : EventStore {
    companion object {
        private const val STREAM_KEY = "sse:events"
    }

    // 실시간 push 용 in-memory sink
    private val sink: Sinks.Many<SseEvent> =
        Sinks.many().multicast().onBackpressureBuffer()

    override fun publish(message: String): Mono<SseEvent> =
        Mono.fromCallable {
            val createdAt = Clock.System.now()
            val ulid = ULID.randomULID(createdAt.toEpochMilliseconds())

            val map =
                mapOf(
                    "ulid" to ulid,
                    "message" to message,
                    "createdAt" to createdAt.toString(),
                )

            val record =
                StreamRecords
                    .newRecord()
                    .ofMap(map)
                    .withStreamKey(STREAM_KEY)

            val recordId: RecordId =
                redisTemplate
                    .opsForStream<String, String>()
                    .add(record)
                    ?: throw IllegalStateException("Failed to add record to Redis Stream")

            val event =
                SseEvent(
                    streamId = recordId.value,
                    ulid = ulid,
                    message = message,
                    createdAt = createdAt,
                )

            val result = sink.tryEmitNext(event)
            if (result.isFailure) {
                LOGGER.warn("Failed to emit SSE event to sink: {}", result)
            }

            event
        }

    override fun streamFrom(lastStreamId: String?): Flux<SseEvent> {
        val missed = loadMissedEvents(lastStreamId)
        val missedFlux = Flux.fromIterable(missed)
        val liveFlux = sink.asFlux()
        return missedFlux.concatWith(liveFlux)
    }

    private fun loadMissedEvents(lastStreamId: String?): List<SseEvent> {
        val ops = redisTemplate.opsForStream<String, String>()

        val range: Range<String> =
            if (lastStreamId == null) {
                Range.unbounded()
            } else {
                Range.rightOpen(lastStreamId, "+")
            }

        val records: List<MapRecord<String, String, String>> = ops.range(STREAM_KEY, range) ?: emptyList()

        return records.map { record ->
            val streamId = record.id.value
            val map = record.value

            val ulid = map["ulid"] ?: "UNKNOWN"
            val message = map["message"] ?: ""
            val createdAt = map["createdAt"]?.let { Instant.parse(it) } ?: Instant.fromEpochMilliseconds(0L)

            SseEvent(
                streamId = streamId,
                ulid = ulid,
                message = message,
                createdAt = createdAt,
            )
        }
    }
}
