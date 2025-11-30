package me.bossm0n5t3r.sse.service

import me.bossm0n5t3r.sse.configuration.LOGGER
import me.bossm0n5t3r.sse.dto.SseEvent
import me.bossm0n5t3r.sse.dto.toSseEvent
import me.bossm0n5t3r.sse.utility.JsonUtil.JSON
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.Sinks
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverOptions
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderRecord
import ulid.ULID
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
@Component
class ReactorKafkaEventStore(
    private val kafkaSender: KafkaSender<String, String>,
    private val receiverOptions: ReceiverOptions<String, String>,
) : EventStore {
    private val topic = "sse-events"

    // 실시간 스트림용 sink
    private val liveSink: Sinks.Many<SseEvent> =
        Sinks.many().multicast().onBackpressureBuffer()

    override fun publish(message: String): Mono<SseEvent> {
        val createdAt = Clock.System.now()
        val ulid = ULID.randomULID(createdAt.toEpochMilliseconds())

        val payload =
            mapOf(
                "ulid" to ulid,
                "message" to message,
                "createdAt" to createdAt.toString(),
            )
        val json = JSON.encodeToString(payload)

        val record = ProducerRecord(topic, ulid, json)

        return kafkaSender
            .send(Mono.just(SenderRecord.create(record, ulid)))
            .next()
            .map { result ->
                val metadata = result.recordMetadata()
                val offset = metadata.offset().toString()

                val event =
                    SseEvent(
                        streamId = offset,
                        ulid = ulid,
                        message = message,
                        createdAt = createdAt,
                    )

                liveSink.tryEmitNext(event).also {
                    if (it.isFailure) LOGGER.warn("live sink emit failed: {}", it)
                }

                event
            }
    }

    override fun streamFrom(lastStreamId: String?): Flux<SseEvent> {
        val startOffset = lastStreamId?.toLongOrNull()?.plus(1) ?: 0L

        val options =
            receiverOptions
                .consumerProperty(ConsumerConfig.GROUP_ID_CONFIG, "sse-reactor-${Uuid.random()}")
                .subscription(listOf(topic))
                .addAssignListener { partitions ->
                    // 각 파티션에 대해 원하는 offset으로 seek
                    partitions.forEach { partition ->
                        LOGGER.info("Assigned partition: {}, seeking to {}", partition, startOffset)
                        // receiverOptions의 assignListener 는 Receiver 내부에서 사용되므로
                        // 실제 seek 는 아래 Receiver에서 수행
                    }
                }

        val receiver = KafkaReceiver.create(options)

        return receiver
            .receive()
            .filter { record ->
                // auto.offset.reset=earliest + 별도 seek를 안 하면,
                // startOffset 이전 것도 올 수 있으니, 앞부분 잘라내기
                record.offset() >= startOffset
            }.map { record ->
                record.value().toSseEvent(record.offset().toString())
            }.doOnNext { event ->
                // liveSink 와 중복이라면 생략해도 되지만,
                // 필요하다면 여기서도 liveSink로 흘려보낼 수 있음
                liveSink.tryEmitNext(event)
            }
    }
}
