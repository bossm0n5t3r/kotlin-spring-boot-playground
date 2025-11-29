package me.bossm0n5t3r.sse.service

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import me.bossm0n5t3r.sse.configuration.LOGGER
import me.bossm0n5t3r.sse.dto.SseEvent
import me.bossm0n5t3r.sse.utility.JsonUtil.JSON
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.TopicPartition
import org.springframework.boot.kafka.autoconfigure.KafkaProperties
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Sinks
import ulid.ULID
import java.time.Duration
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
@Component
class KafkaEventStore(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val kafkaProperties: KafkaProperties,
) : EventStore {
    private val topic = "sse-events"

    private val sink: Sinks.Many<SseEvent> =
        Sinks.many().multicast().onBackpressureBuffer()

    override fun publish(message: String): SseEvent {
        val createdAt = Clock.System.now()
        val ulid = ULID.randomULID(createdAt.toEpochMilliseconds())

        // Kafka에 기록할 payload (JSON)
        val payload =
            mapOf(
                "ulid" to ulid,
                "message" to message,
                "createdAt" to createdAt.toString(),
            )

        val json = JSON.encodeToString(payload)

        val future = kafkaTemplate.send(topic, ulid, json)

        // send() 결과에서 offset을 가져와 SSE id로 사용
        val result = future.get() // 데모용. 실서비스에선 비동기 콜백 권장
        val offset = result.recordMetadata.offset().toString()

        val event =
            SseEvent(
                streamId = offset,
                ulid = ulid,
                message = message,
                createdAt = createdAt,
            )

        sink.tryEmitNext(event).also {
            if (it.isFailure) LOGGER.warn("Kafka SSE sink emit failed: {}", it)
        }

        return event
    }

    override fun streamFrom(lastStreamId: String?): Flux<SseEvent> {
        val startOffset = lastStreamId?.toLongOrNull()?.plus(1) ?: 0L

        val missedFlux = Flux.fromIterable(loadMissedEventsFromKafka(startOffset))
        val liveFlux = sink.asFlux()

        return missedFlux.concatWith(liveFlux)
    }

    private fun loadMissedEventsFromKafka(startOffset: Long): List<SseEvent> {
        val consumerProps = kafkaProperties.buildConsumerProperties().toMutableMap()
        consumerProps[ConsumerConfig.GROUP_ID_CONFIG] = "sse-kafka-replay-${Uuid.random()}"

        val consumer = KafkaConsumer<String, String>(consumerProps)
        consumer.assign(listOf(TopicPartition(topic, 0)))

        // 시작 offset 설정
        consumer.seek(TopicPartition(topic, 0), startOffset)

        val result = mutableListOf<SseEvent>()

        // 간단하게 한 번 poll 해서 히스토리 일부만 가져오는 예제 (실서비스에선 loop 설계 필요)
        val records = consumer.poll(Duration.ofMillis(500))
        for (record in records) {
            val offset = record.offset().toString()
            val node = JSON.decodeFromString<JsonObject>(record.value())
            val ulid = node["ulid"]?.jsonPrimitive?.content.toString()
            val message = node["message"]?.jsonPrimitive?.content.toString()
            val createdAt = Instant.parse(node["createdAt"]?.jsonPrimitive?.content.toString())

            result +=
                SseEvent(
                    streamId = offset,
                    ulid = ulid,
                    message = message,
                    createdAt = createdAt,
                )
        }

        consumer.close()
        return result
    }
}
