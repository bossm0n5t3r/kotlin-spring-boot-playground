package me.bossm0n5t3r.mcpserver

import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.client.RestClient
import org.springframework.web.client.toEntity

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class McpStreamableHttpTest {
    @LocalServerPort private var port: Int = 0

    @Test
    fun initialize_returns_session_id_and_server_info() {
        val response =
            RestClient.create("http://localhost:$port")
                .post()
                .uri("/mcp")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_EVENT_STREAM)
                .body(INITIALIZE_REQUEST)
                .retrieve()
                .toEntity<String>()

        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.headers.getFirst("Mcp-Session-Id"))
        val body = assertNotNull(response.body)
        assertTrue(body.contains("\"name\":\"playground-mcp-server\""), body)
    }

    companion object {
        private val INITIALIZE_REQUEST =
            """
            {
              "jsonrpc": "2.0",
              "id": 1,
              "method": "initialize",
              "params": {
                "protocolVersion": "2025-06-18",
                "capabilities": {},
                "clientInfo": { "name": "test-client", "version": "0.0.1" }
              }
            }
            """
                .trimIndent()
    }
}
