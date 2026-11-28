package me.bossm0n5t3r.mcpserver.project

import io.modelcontextprotocol.client.McpClient
import io.modelcontextprotocol.client.McpSyncClient
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport
import io.modelcontextprotocol.spec.McpSchema
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import tools.jackson.databind.JsonNode
import tools.jackson.databind.json.JsonMapper

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProjectToolsSchemaTest {
    @LocalServerPort private var port: Int = 0

    @Autowired private lateinit var jsonMapper: JsonMapper

    private lateinit var client: McpSyncClient

    @BeforeEach
    fun setUp() {
        val transport =
            HttpClientStreamableHttpTransport.builder("http://localhost:$port")
                .endpoint("/mcp")
                .build()
        client = McpClient.sync(transport).build()
        client.initialize()
    }

    @AfterEach
    fun tearDown() {
        client.close()
    }

    @Test
    fun listTools_returns_project_tools() {
        val tools = client.listTools().tools().associateBy { it.name() }

        assertEquals(setOf("list_projects", "get_project"), tools.keys)
        assertEquals("List all projects.", tools.getValue("list_projects").description())
        assertEquals("Get a project by its ID.", tools.getValue("get_project").description())
    }

    @Test
    fun listTools_returns_read_only_annotations() {
        val tools = client.listTools().tools()

        assertEquals(2, tools.size)
        tools.forEach { tool ->
            val annotations = assertNotNull(tool.annotations(), tool.name())
            assertEquals(true, annotations.readOnlyHint(), tool.name())
            assertEquals(false, annotations.destructiveHint(), tool.name())
            assertEquals(true, annotations.idempotentHint(), tool.name())
            assertEquals(false, annotations.openWorldHint(), tool.name())
        }
    }

    @Test
    fun listTools_returns_empty_input_schema_for_list_projects() {
        val schema = inputSchema("list_projects")

        assertEquals("object", schema.path("type").asString())
        val properties = schema.path("properties")
        assertTrue(properties.isObject)
        assertTrue(properties.isEmpty)
    }

    @Test
    fun listTools_returns_required_string_id_in_get_project_schema() {
        val schema = inputSchema("get_project")

        assertEquals("object", schema.path("type").asString())
        val properties = schema.path("properties")
        assertEquals(setOf("id"), properties.propertyNames().toSet())
        assertEquals("string", properties.path("id").path("type").asString())
        assertTrue(properties.path("id").path("description").asString().isNotBlank())
        assertEquals(listOf("id"), schema.path("required").values().map { it.asString() })
    }

    private fun inputSchema(toolName: String): JsonNode =
        jsonMapper.valueToTree(tool(toolName).inputSchema())

    private fun tool(name: String): McpSchema.Tool =
        client.listTools().tools().single { it.name() == name }
}
