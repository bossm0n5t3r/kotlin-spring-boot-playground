package me.bossm0n5t3r.contentnegotiation.service

import me.bossm0n5t3r.contentnegotiation.configuration.Constants
import me.bossm0n5t3r.contentnegotiation.dto.PayloadData
import org.springframework.stereotype.Service

@Service
class PayloadGeneratorService {
    fun generateSmall(): PayloadData =
        PayloadData(
            id = Constants.FIXED_ID_FOR_TESTING,
            content = "Small payload content",
        )

    fun generateMedium(): PayloadData {
        val repeatedContent = "This is a repeated string for medium payload. ".repeat(20)
        val dataList = List(50) { "Item $it: $repeatedContent" }
        return PayloadData(
            id = Constants.FIXED_ID_FOR_TESTING,
            content = repeatedContent,
            data = dataList,
        )
    }

    fun generateLarge(): PayloadData {
        val randomData = List(100) { "Unique item item-index-$it" }
        val repeatedData = List(100) { "Repeated item for high compression efficiency" }
        return PayloadData(
            id = Constants.FIXED_ID_FOR_TESTING,
            content = "Large payload with mixed entropy content. ".repeat(50),
            data = randomData + repeatedData,
        )
    }
}
