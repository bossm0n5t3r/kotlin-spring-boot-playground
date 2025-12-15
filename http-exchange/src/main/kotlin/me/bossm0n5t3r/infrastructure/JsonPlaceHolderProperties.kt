package me.bossm0n5t3r.infrastructure

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("clients.json-place-holder")
data class JsonPlaceHolderProperties(
    val baseUrl: String,
)
