package me.bossm0n5t3r.contentnegotiation

import org.springframework.stereotype.Service
import java.util.UUID

@Service
class PayloadGeneratorService {
    fun generateSmall(): PayloadData =
        PayloadData(
            id = UUID.randomUUID().toString(),
            content = "Small payload content",
        )

    fun generateMedium(): PayloadData {
        val repeatedContent = "This is a repeated string for medium payload. ".repeat(20)
        val dataList = List(50) { "Item $it: $repeatedContent" }
        return PayloadData(
            id = UUID.randomUUID().toString(),
            content = repeatedContent,
            data = dataList,
        )
    }

    fun generateLarge(): PayloadData {
        val randomData = List(100) { "Unique item ${UUID.randomUUID()}" }
        val repeatedData = List(100) { "Repeated item for high compression efficiency" }
        return PayloadData(
            id = UUID.randomUUID().toString(),
            content = "Large payload with mixed entropy content. ".repeat(50),
            data = randomData + repeatedData,
        )
    }
}

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
