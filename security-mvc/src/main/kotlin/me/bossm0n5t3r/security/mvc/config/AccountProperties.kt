package me.bossm0n5t3r.security.mvc.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("account")
data class AccountProperties(
    val url: String,
)
