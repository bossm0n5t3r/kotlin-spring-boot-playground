package me.bossm0n5t3r.contentnegotiation.dto

import me.bossm0n5t3r.contentnegotiation.PayloadProto

data class PayloadData(
    val id: String,
    val content: String,
    val data: List<String> = emptyList(),
) {
    fun toDto() = PayloadDto(id, content, data)

    fun toProto(): PayloadProto =
        PayloadProto
            .newBuilder()
            .setId(id)
            .setContent(content)
            .addAllData(data)
            .build()
}
