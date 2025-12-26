package me.bossm0n5t3r.contentnegotiation

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController

@RestController
class PayloadController(
    private val payloadGeneratorService: PayloadGeneratorService,
) {
    @GetMapping("/payload/small")
    fun getSmallPayload(
        @RequestHeader("Accept") accept: String?,
    ): ResponseEntity<Any> = handlePayload(accept, payloadGeneratorService.generateSmall())

    @GetMapping("/payload/medium")
    fun getMediumPayload(
        @RequestHeader("Accept") accept: String?,
    ): ResponseEntity<Any> = handlePayload(accept, payloadGeneratorService.generateMedium())

    @GetMapping("/payload/large")
    fun getLargePayload(
        @RequestHeader("Accept") accept: String?,
    ): ResponseEntity<Any> = handlePayload(accept, payloadGeneratorService.generateLarge())

    private fun handlePayload(
        accept: String?,
        data: PayloadData,
    ): ResponseEntity<Any> {
        val varyHeader = "Accept, Accept-Encoding"
        return when {
            accept?.contains("application/x-msgpack") == true -> {
                ResponseEntity.ok().header("Vary", varyHeader).body(data.toDto())
            }
            accept?.contains("application/x-protobuf") == true || accept?.contains("application/protobuf") == true -> {
                ResponseEntity.ok().header("Vary", varyHeader).body(data.toProto())
            }
            else -> {
                ResponseEntity.ok().header("Vary", varyHeader).body(data.toDto())
            }
        }
    }
}

data class PayloadDto(
    val id: String,
    val content: String,
    val data: List<String> = emptyList(),
)
