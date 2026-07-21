package me.bossm0n5t3r.readability4k.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "readability4k")
data class ReadabilityProperties(val cors: Cors = Cors(), val http: Http = Http()) {
    data class Cors(val origins: List<String> = listOf("*"))

    data class Http(
        val connectTimeoutSeconds: Long = 10,
        val requestTimeoutSeconds: Long = 30,
        val userAgent: String =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
    )
}
