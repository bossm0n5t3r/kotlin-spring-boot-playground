package me.bossm0n5t3r.mcpserver.project

import org.springframework.stereotype.Repository

/** In-memory fixture with fictional test data only. */
@Repository
class ProjectRepository {
    private val projects =
        listOf(
                Project(
                    id = "project-alpha",
                    name = "Alpha",
                    ownerId = "user-alice",
                    tenantId = "tenant-a",
                ),
                Project(
                    id = "project-beta",
                    name = "Beta",
                    ownerId = "user-bob",
                    tenantId = "tenant-a",
                ),
                Project(
                    id = "project-gamma",
                    name = "Gamma",
                    ownerId = "user-admin",
                    tenantId = "tenant-b",
                ),
            )
            .associateBy { it.id }

    fun findAll(): List<Project> = projects.values.toList()

    fun findById(id: String): Project? = projects[id]
}
