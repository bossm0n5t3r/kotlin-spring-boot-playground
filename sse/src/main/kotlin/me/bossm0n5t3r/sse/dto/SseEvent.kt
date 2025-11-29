package me.bossm0n5t3r.sse.dto

import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
@Serializable
data class SseEvent(
    val streamId: String, // Redis Streams ID (SSE용 id)
    val ulid: String, // 외부/로그용 ID
    val message: String,
    val createdAt: Instant,
)
