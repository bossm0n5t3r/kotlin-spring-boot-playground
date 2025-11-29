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
import reactor.core.publisher.Sinks
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

    override fun publish(message: String): SseEvent {
        val createdAt = Clock.System.now()
        val map =
            mapOf(
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
                id = recordId.value,
                message = message,
                createdAt = createdAt,
            )

        // 실시간 구독자에게도 push
        val result = sink.tryEmitNext(event)
        if (result.isFailure) {
            LOGGER.warn("Failed to emit event to sink: {}", result)
        }

        return event
    }

    override fun streamFrom(lastEventId: String?): Flux<SseEvent> {
        // 1) Redis Streams 에서 과거 이벤트 조회
        val missedEvents: List<SseEvent> = loadMissedEvents(lastEventId)

        val missedFlux = Flux.fromIterable(missedEvents)
        val liveFlux = sink.asFlux()

        // 2) 과거 이벤트 다 보내고 -> 이후에는 실시간 스트림
        return missedFlux.concatWith(liveFlux)
    }

    private fun loadMissedEvents(lastEventId: String?): List<SseEvent> {
        val ops = redisTemplate.opsForStream<String, String>()

        val records: List<MapRecord<String, String, String>> =
            if (lastEventId == null) {
                // 처음 접속: 스트림 전체 (실서비스라면 TTL/최대 길이 고려)
                ops.range(STREAM_KEY, Range.unbounded()) ?: emptyList()
            } else {
                // lastEventId 보다 큰 ID 들만
                ops.range(STREAM_KEY, Range.rightOpen(lastEventId, "+")) ?: emptyList()
            }

        return records.map { record ->
            val id = record.id.value
            val map = record.value

            val message = map["message"] ?: ""
            val createdAt = map["createdAt"]?.let { Instant.parse(it) } ?: Instant.fromEpochMilliseconds(0L)

            SseEvent(
                id = id,
                message = message,
                createdAt = createdAt,
            )
        }
    }
}
