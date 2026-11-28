package me.bossm0n5t3r.mcpserver.transport

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActionsDsl
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

@SpringBootTest
@AutoConfigureMockMvc
class McpTransportSecurityTest {
    @Autowired private lateinit var mockMvc: MockMvc

    @Test
    fun initialize_returns_200_if_origin_is_allowed() {
        initialize(origin = "http://localhost:6274").andExpect { status { isOk() } }
    }

    @Test
    fun initialize_returns_403_if_origin_is_not_allowed() {
        initialize(origin = "http://evil.example").andExpect { status { isForbidden() } }
    }

    @Test
    fun initialize_returns_421_if_host_is_not_allowed() {
        initialize(host = "evil.example:8087").andExpect {
            status { isEqualTo(HttpStatus.MISDIRECTED_REQUEST.value()) }
        }
    }

    @Test
    fun openStream_returns_403_if_origin_is_not_allowed() {
        openStream(origin = "http://evil.example").andExpect { status { isForbidden() } }
    }

    @Test
    fun openStream_returns_421_if_host_is_not_allowed() {
        openStream(host = "evil.example:8087").andExpect {
            status { isEqualTo(HttpStatus.MISDIRECTED_REQUEST.value()) }
        }
    }

    @Test
    fun terminateSession_returns_403_if_origin_is_not_allowed() {
        terminateSession(origin = "http://evil.example").andExpect { status { isForbidden() } }
    }

    @Test
    fun terminateSession_returns_421_if_host_is_not_allowed() {
        terminateSession(host = "evil.example:8087").andExpect {
            status { isEqualTo(HttpStatus.MISDIRECTED_REQUEST.value()) }
        }
    }

    private fun initialize(
        origin: String? = null,
        host: String = "localhost:8087",
    ): ResultActionsDsl =
        mockMvc.post("/mcp") {
            contentType = MediaType.APPLICATION_JSON
            accept(MediaType.APPLICATION_JSON, MediaType.TEXT_EVENT_STREAM)
            header(HttpHeaders.HOST, host)
            origin?.let { header(HttpHeaders.ORIGIN, it) }
            content = INITIALIZE_REQUEST
        }

    private fun openStream(
        origin: String? = null,
        host: String = "localhost:8087",
    ): ResultActionsDsl =
        mockMvc.get("/mcp") {
            accept(MediaType.TEXT_EVENT_STREAM)
            header(HttpHeaders.HOST, host)
            header(MCP_SESSION_ID_HEADER, UNKNOWN_SESSION_ID)
            origin?.let { header(HttpHeaders.ORIGIN, it) }
        }

    private fun terminateSession(
        origin: String? = null,
        host: String = "localhost:8087",
    ): ResultActionsDsl =
        mockMvc.delete("/mcp") {
            header(HttpHeaders.HOST, host)
            header(MCP_SESSION_ID_HEADER, UNKNOWN_SESSION_ID)
            origin?.let { header(HttpHeaders.ORIGIN, it) }
        }

    companion object {
        private const val MCP_SESSION_ID_HEADER = "Mcp-Session-Id"

        // With allowed headers this unknown session returns 404, so 403/421 proves header
        // validation.
        private const val UNKNOWN_SESSION_ID = "unknown-session"

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
