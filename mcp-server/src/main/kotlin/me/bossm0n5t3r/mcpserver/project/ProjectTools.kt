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
    ): Project =
        projectRepository.findById(id) ?: throw NoSuchElementException("Project not found: $id")
}
