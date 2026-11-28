package me.bossm0n5t3r.mcpserver.project

import io.modelcontextprotocol.spec.McpError
import io.modelcontextprotocol.spec.McpSchema
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import me.bossm0n5t3r.mcpserver.McpClientTestSupport
import org.junit.jupiter.api.Test

class ProjectToolsCallErrorTest : McpClientTestSupport() {
    @Test
    fun callTool_throws_McpError_with_invalid_params_if_tool_is_unknown() {
        val error = assertFailsWith<McpError> { callTool("no_such_tool") }

        assertEquals(McpSchema.ErrorCodes.INVALID_PARAMS, error.jsonRpcError.code())
        assertFalse(error.jsonRpcError.message().contains("no_such_tool"))
    }

    @Test
    fun callTool_returns_error_result_if_required_argument_is_missing() {
        val result = callTool("get_project")

        assertRejectedBeforeToolExecution(result)
    }

    @Test
    fun callTool_returns_error_result_if_argument_type_is_wrong() {
        val result = callTool("get_project", mapOf("id" to 123))

        assertRejectedBeforeToolExecution(result)
    }

    @Test
    fun callTool_returns_error_result_without_echoing_input_if_id_format_is_invalid() {
        val invalidId = "<script>alert(1)</script>"

        val result = callTool("get_project", mapOf("id" to invalidId))

        assertTrue(result.isError ?: false)
        assertContains(result.text(), "Invalid project id format")
        assertFalse(result.text().contains(invalidId))
    }

    @Test
    fun callTool_returns_error_result_if_project_not_found() {
        val result = callTool("get_project", mapOf("id" to "project-unknown"))

        assertTrue(result.isError ?: false)
        assertContains(result.text(), "Project not found: project-unknown")
    }

    private fun assertRejectedBeforeToolExecution(result: McpSchema.CallToolResult) {
        assertTrue(result.isError ?: false)
        assertFalse(result.text().contains("Invalid project id format"))
        assertFalse(result.text().contains("Project not found"))
    }
}
