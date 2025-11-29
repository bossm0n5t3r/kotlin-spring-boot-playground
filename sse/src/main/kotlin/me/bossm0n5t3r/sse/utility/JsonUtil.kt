package me.bossm0n5t3r.sse.utility

import kotlinx.serialization.json.Json

object JsonUtil {
    val JSON =
        Json {
            encodeDefaults = true
        }
}
