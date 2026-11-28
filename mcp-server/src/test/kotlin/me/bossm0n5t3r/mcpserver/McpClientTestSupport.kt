package me.bossm0n5t3r.mcpserver

import io.modelcontextprotocol.client.McpClient
import io.modelcontextprotocol.client.McpSyncClient
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import tools.jackson.databind.json.JsonMapper

/** Starts the server on a random port and connects a real MCP client over Streamable HTTP. */
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
}
