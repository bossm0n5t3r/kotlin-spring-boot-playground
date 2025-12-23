package me.bossm0n5t3r.dto

import com.fasterxml.jackson.annotation.JsonFormat
import io.github.bossm0n5t3r.ksx.jvm.time.LocalDateTimeSerializer
import io.github.bossm0n5t3r.ksx.jvm.time.OffsetDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.time.OffsetDateTime

@Serializable
data class DateTimeDtoUsingLibrary(
    val name: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime,
    @Serializable(with = OffsetDateTimeSerializer::class)
    @field:JsonFormat(pattern = OFFSET_DATE_TIME_PATTERN, with = [JsonFormat.Feature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE])
    val updatedAt: OffsetDateTime,
)
