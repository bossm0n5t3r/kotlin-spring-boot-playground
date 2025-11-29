package me.bossm0n5t3r.sse.publisher

import me.bossm0n5t3r.sse.configuration.LOGGER
import me.bossm0n5t3r.sse.service.EventStore
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@EnableScheduling
@Component
class RedisPublisher(
    @param:Qualifier("redisStreamEventStore")
    override val eventStore: EventStore,
) : EventPublisher {
    @Scheduled(fixedRate = 5_000)
    override fun publish() {
        val event = eventStore.publish("tick at ${Clock.System.now()}")
        LOGGER.info("published event: {}", event)
    }
}
