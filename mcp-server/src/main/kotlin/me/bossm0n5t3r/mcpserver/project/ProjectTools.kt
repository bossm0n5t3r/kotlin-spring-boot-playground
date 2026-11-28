package me.bossm0n5t3r.mcpserver.project

import org.springframework.ai.mcp.annotation.McpTool
import org.springframework.ai.mcp.annotation.McpToolParam
import org.springframework.stereotype.Component

@Component
class ProjectTools(
    private val projectRepository: ProjectRepository,
) {
    @McpTool(
        name = "list_projects",
        description = "List all projects.",
        annotations =
            McpTool.McpAnnotations(
                readOnlyHint = true,
                destructiveHint = false,
                idempotentHint = true,
                openWorldHint = false,
            ),
    )
    fun listProjects(): List<Project> = projectRepository.findAll()

    @McpTool(
        name = "get_project",
        description = "Get a project by its ID.",
        annotations =
            McpTool.McpAnnotations(
                readOnlyHint = true,
                destructiveHint = false,
                idempotentHint = true,
                openWorldHint = false,
            ),
    )
    fun getProject(
        @McpToolParam(description = "Project ID, e.g. project-alpha") id: String,
    ): Project {
        // Validate before echoing: tool results are fed into the LLM context.
        require(PROJECT_ID_PATTERN.matches(id)) { "Invalid project id format" }
        return projectRepository.findById(id)
            ?: throw NoSuchElementException("Project not found: $id")
    }

    companion object {
        private val PROJECT_ID_PATTERN = Regex("^[a-z0-9-]{1,64}$")
    }
}
