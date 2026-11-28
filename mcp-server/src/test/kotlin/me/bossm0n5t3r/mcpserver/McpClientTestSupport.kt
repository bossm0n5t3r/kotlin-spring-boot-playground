package me.bossm0n5t3r.mcpserver

import io.modelcontextprotocol.client.McpClient
import io.modelcontextprotocol.client.McpSyncClient
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport
import io.modelcontextprotocol.spec.McpSchema
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import tools.jackson.databind.json.JsonMapper

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class McpClientTestSupport {
    @LocalServerPort private var port: Int = 0

    @Autowired protected lateinit var jsonMapper: JsonMapper

    protected lateinit var client: McpSyncClient

    @BeforeEach
    fun connectClient() {
        val transport =
            HttpClientStreamableHttpTransport.builder("http://localhost:$port")
                .endpoint("/mcp")
                .build()
        client = McpClient.sync(transport).build()
        client.initialize()
    }

    @AfterEach
    fun closeClient() {
        client.close()
    }

    protected fun callTool(
        name: String,
        arguments: Map<String, Any> = emptyMap(),
    ): McpSchema.CallToolResult =
        client.callTool(McpSchema.CallToolRequest.builder(name).arguments(arguments).build())

    protected fun McpSchema.CallToolResult.text(): String =
        (content().single() as McpSchema.TextContent).text()
}
