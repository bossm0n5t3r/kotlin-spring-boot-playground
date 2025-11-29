package me.bossm0n5t3r.sse.service

import me.bossm0n5t3r.sse.configuration.LOGGER
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@EnableScheduling
@Component
class EventPublisher(
    private val eventStore: EventStore,
) {
    @Scheduled(fixedRate = 5_000)
    fun publishTick() {
        val event = eventStore.publish("tick at ${Clock.System.now()}")
        LOGGER.info("published event: {}", event)
    }
}
