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

    @Test
    fun callTool_returns_project_if_undeclared_argument_is_given() {
        val result = callTool("get_project", mapOf("id" to "project-alpha", "extra" to 1))

        assertFalse(result.isError ?: false)
        assertEquals("project-alpha", textJson(result).path("id").asString())
    }

    private fun textJson(result: McpSchema.CallToolResult): JsonNode =
        jsonMapper.readTree(result.text())
}
