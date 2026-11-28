package me.bossm0n5t3r.mcpserver.transport

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("playground.mcp.transport")
data class McpTransportSecurityProperties(
    val allowedOrigins: List<String>,
    val allowedHosts: List<String>,
)
