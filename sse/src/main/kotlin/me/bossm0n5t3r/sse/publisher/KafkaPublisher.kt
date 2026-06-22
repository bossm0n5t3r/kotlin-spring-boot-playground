package me.bossm0n5t3r.sse.publisher

import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import me.bossm0n5t3r.sse.configuration.LOGGER
import me.bossm0n5t3r.sse.service.EventStore
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@OptIn(ExperimentalTime::class)
@Component
class KafkaPublisher(@param:Qualifier("kafkaEventStore") override val eventStore: EventStore) :
    EventPublisher {
    @Scheduled(fixedRate = 5000)
    override fun publish() {
        eventStore.publish("[kafka] tick at ${Clock.System.now()}").subscribe {
            LOGGER.info("published Kafka SSE event: {}", it)
        }
    }
}
