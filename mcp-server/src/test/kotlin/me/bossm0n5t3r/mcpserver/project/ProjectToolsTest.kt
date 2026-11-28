package me.bossm0n5t3r.mcpserver.project

import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.junit.jupiter.api.Test

class ProjectToolsTest {
    private val projectRepository = ProjectRepository()
    private val projectTools = ProjectTools(projectRepository)

    @Test
    fun listProjects_returns_all_projects() {
        assertEquals(
            listOf("project-alpha", "project-beta", "project-gamma"),
            projectTools.listProjects().map { it.id },
        )
    }

    @Test
    fun getProject_returns_project_with_given_id() {
        val project = projectTools.getProject("project-alpha")

        assertEquals("project-alpha", project.id)
        assertEquals("user-alice", project.ownerId)
    }

    @Test
    fun getProject_throws_NoSuchElementException_if_project_not_found() {
        assertFailsWith<NoSuchElementException> { projectTools.getProject("unknown") }
    }

    @Test
    fun getProject_throws_IllegalArgumentException_if_id_is_blank() {
        assertFailsWith<IllegalArgumentException> { projectTools.getProject("") }
    }

    @Test
    fun getProject_throws_IllegalArgumentException_if_id_has_invalid_characters() {
        assertFailsWith<IllegalArgumentException> { projectTools.getProject("../project-alpha") }
    }

    @Test
    fun getProject_throws_IllegalArgumentException_if_id_has_uppercase_letters() {
        assertFailsWith<IllegalArgumentException> { projectTools.getProject("Project-Alpha") }
    }

    @Test
    fun getProject_throws_NoSuchElementException_if_id_has_max_length() {
        assertFailsWith<NoSuchElementException> { projectTools.getProject("a".repeat(64)) }
    }

    @Test
    fun getProject_throws_IllegalArgumentException_if_id_is_too_long() {
        assertFailsWith<IllegalArgumentException> { projectTools.getProject("a".repeat(65)) }
    }
}
