package me.bossm0n5t3r.mcpserver.project

import io.modelcontextprotocol.spec.McpSchema
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import me.bossm0n5t3r.mcpserver.McpClientTestSupport
import org.junit.jupiter.api.Test
import tools.jackson.databind.JsonNode

class ProjectToolsCallTest : McpClientTestSupport() {
    @Test
    fun callTool_returns_all_projects_for_list_projects() {
        val result = callTool("list_projects")

        assertFalse(result.isError ?: false)
        val projects = textJson(result)
        assertTrue(projects.isArray)
        assertEquals(
            listOf("project-alpha", "project-beta", "project-gamma"),
            projects.values().map { it.path("id").asString() },
        )
    }

    @Test
    fun callTool_returns_project_for_get_project() {
        val result = callTool("get_project", mapOf("id" to "project-alpha"))

        assertFalse(result.isError ?: false)
        val project = textJson(result)
        assertEquals("project-alpha", project.path("id").asString())
        assertEquals("Alpha", project.path("name").asString())
        assertEquals("user-alice", project.path("ownerId").asString())
        assertEquals("tenant-a", project.path("tenantId").asString())
    }

    private fun callTool(
        name: String,
        arguments: Map<String, Any> = emptyMap(),
    ): McpSchema.CallToolResult =
        client.callTool(McpSchema.CallToolRequest.builder(name).arguments(arguments).build())

    private fun textJson(result: McpSchema.CallToolResult): JsonNode {
        val content = result.content().single() as McpSchema.TextContent
        return jsonMapper.readTree(content.text())
    }
}
