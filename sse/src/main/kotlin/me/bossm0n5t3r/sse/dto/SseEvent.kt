package me.bossm0n5t3r.sse.dto

import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
@Serializable
data class SseEvent(
    val id: String,
    val message: String,
    val createdAt: Instant = Clock.System.now(),
)
