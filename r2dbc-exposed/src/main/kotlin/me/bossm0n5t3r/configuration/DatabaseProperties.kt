package me.bossm0n5t3r.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.datasource")
data class DatabaseProperties(
    val master: ConnectionProperties,
    val slave: ConnectionProperties,
) {
    data class ConnectionProperties(
        val r2dbc: R2dbcProperties,
    )

    data class R2dbcProperties(
        val url: String,
    )
}
