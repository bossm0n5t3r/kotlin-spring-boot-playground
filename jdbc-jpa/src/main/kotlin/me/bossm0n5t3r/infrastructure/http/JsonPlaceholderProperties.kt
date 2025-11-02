package me.bossm0n5t3r.infrastructure.http

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "clients.jsonplaceholder")
data class JsonPlaceholderProperties(
    val baseUrl: String,
)
