package me.bossm0n5t3r.mcpserver.transport

import io.modelcontextprotocol.server.transport.DefaultServerTransportSecurityValidator
import org.springframework.ai.mcp.customizer.McpServerCustomizer
import org.springframework.ai.mcp.server.webmvc.transport.WebMvcStreamableServerTransportProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class McpTransportSecurityConfig {
    /**
     * Rejects requests whose `Origin` (403) or `Host` (421) header is not allow-listed, to protect
     * the local MCP endpoint against DNS rebinding and cross-origin browser requests.
     */
    @Bean
    fun streamableTransportSecurityCustomizer(
        properties: McpTransportSecurityProperties,
    ): McpServerCustomizer<WebMvcStreamableServerTransportProvider.Builder> {
        val validator =
            DefaultServerTransportSecurityValidator.builder()
                .allowedOrigins(properties.allowedOrigins)
                .allowedHosts(properties.allowedHosts)
                .build()
        return McpServerCustomizer { builder -> builder.securityValidator(validator) }
    }
}
