package me.bossm0n5t3r.sse.service

import me.bossm0n5t3r.sse.configuration.LOGGER
import me.bossm0n5t3r.sse.dto.SseEvent
import me.bossm0n5t3r.sse.dto.toSseEvent
import me.bossm0n5t3r.sse.utility.JsonUtil.JSON
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.TopicPartition
import org.springframework.boot.kafka.autoconfigure.KafkaProperties
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import ulid.ULID
import java.time.Duration
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
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

    override fun publish(message: String): Mono<SseEvent> {
        val createdAt = Clock.System.now()
        val ulid = ULID.randomULID(createdAt.toEpochMilliseconds())

        // JSON payload
        val payload =
            mapOf(
                "ulid" to ulid,
                "message" to message,
                "createdAt" to createdAt.toString(),
            )
        val json = JSON.encodeToString(payload)

        // KafkaProducerRecord
        val future = kafkaTemplate.send(topic, ulid, json)

        return Mono
            .fromFuture { future }
            .map { sendResult ->
                val offset = sendResult.recordMetadata.offset().toString()

                val event =
                    SseEvent(
                        streamId = offset,
                        ulid = ulid,
                        message = message,
                        createdAt = createdAt,
                    )

                // push to sink
                sink.tryEmitNext(event).also {
                    if (it.isFailure) LOGGER.warn("Kafka SSE sink emit failed: {}", it)
                }

                event
            }
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

        consumer.seek(TopicPartition(topic, 0), startOffset)

        val result = mutableListOf<SseEvent>()

        val records = consumer.poll(Duration.ofMillis(500))
        for (record in records) {
            val offset = record.offset().toString()
            result += record.value().toSseEvent(offset)
        }

        consumer.close()
        return result
    }
}
