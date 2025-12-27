package me.bossm0n5t3r.contentnegotiation.controller

import me.bossm0n5t3r.contentnegotiation.Constants
import me.bossm0n5t3r.contentnegotiation.dto.PayloadData
import me.bossm0n5t3r.contentnegotiation.service.PayloadGeneratorService
import org.springframework.http.MediaType
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
        @RequestHeader(Constants.ACCEPT_HEADER) accept: String?,
    ): ResponseEntity<Any> = handlePayload(accept, payloadGeneratorService.generateSmall())

    @GetMapping("/payload/medium")
    fun getMediumPayload(
        @RequestHeader(Constants.ACCEPT_HEADER) accept: String?,
    ): ResponseEntity<Any> = handlePayload(accept, payloadGeneratorService.generateMedium())

    @GetMapping("/payload/large")
    fun getLargePayload(
        @RequestHeader(Constants.ACCEPT_HEADER) accept: String?,
    ): ResponseEntity<Any> = handlePayload(accept, payloadGeneratorService.generateLarge())

    private fun handlePayload(
        accept: String?,
        data: PayloadData,
    ): ResponseEntity<Any> =
        when {
            accept?.contains(Constants.MSGPACK_MEDIA_TYPE) == true -> {
                ResponseEntity
                    .ok()
                    .header(
                        Constants.VARY_HEADER,
                        Constants.VARY_HEADER_VALUE,
                    ).contentType(
                        MediaType.parseMediaType(Constants.MSGPACK_MEDIA_TYPE),
                    ).body(data.toDto())
            }

            accept?.contains(Constants.PROTOBUF_MEDIA_TYPE) == true || accept?.contains(Constants.PROTOBUF_MEDIA_TYPE_LEGACY) == true -> {
                ResponseEntity
                    .ok()
                    .header(
                        Constants.VARY_HEADER,
                        Constants.VARY_HEADER_VALUE,
                    ).contentType(
                        MediaType.parseMediaType(Constants.PROTOBUF_MEDIA_TYPE),
                    ).body(data.toProto())
            }

            else -> {
                ResponseEntity
                    .ok()
                    .header(
                        Constants.VARY_HEADER,
                        Constants.VARY_HEADER_VALUE,
                    ).contentType(MediaType.APPLICATION_JSON)
                    .body(data.toDto())
            }
        }
}