package me.bossm0n5t3r.sse.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import me.bossm0n5t3r.sse.utility.JsonUtil.JSON
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

@OptIn(ExperimentalTime::class)
fun String.toSseEvent(offset: String): SseEvent {
    val node = JSON.decodeFromString<JsonObject>(this)

    val ulid = node["ulid"]?.jsonPrimitive?.content.toString()
    val message = node["message"]?.jsonPrimitive?.content.toString()
    val createdAt = Instant.parse(node["createdAt"]?.jsonPrimitive?.content.toString())

    return SseEvent(
        streamId = offset,
        ulid = ulid,
        message = message,
        createdAt = createdAt,
    )
}
