package me.bossm0n5t3r.txroutingdatasource.presentation

import io.mockk.every
import io.mockk.mockk
import me.bossm0n5t3r.txroutingdatasource.domain.entity.User
import me.bossm0n5t3r.txroutingdatasource.exception.ExceptionHandler
import me.bossm0n5t3r.txroutingdatasource.service.UserService
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

class UserControllerTest {
    private val userService: UserService = mockk()
    private val mockMvc: MockMvc =
        MockMvcBuilders
            .standaloneSetup(UserController(userService))
            .setControllerAdvice(ExceptionHandler())
            .build()

    @Test
    fun `should create user`() {
        val user = User(name = "master", email = "master@example.com")
        ReflectionTestUtils.setField(user, "id", 1L)
        every { userService.createUser("master", "master@example.com") } returns user

        mockMvc
            .perform(
                post("/api/v1/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {"name":"master","email":"master@example.com"}
                        """.trimIndent(),
                    ),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
    }

    @Test
    fun `should get user`() {
        val user = User(name = "slave", email = "slave@example.com")
        ReflectionTestUtils.setField(user, "id", 2L)
        every { userService.getUser(2L) } returns user

        mockMvc
            .perform(get("/api/v1/users/2"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(2))
            .andExpect(jsonPath("$.name").value("slave"))
            .andExpect(jsonPath("$.email").value("slave@example.com"))
    }

    @Test
    fun `should return 404 when user not found`() {
        every { userService.getUser(999L) } returns null

        mockMvc
            .perform(get("/api/v1/users/999"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `should update user`() {
        val updated = User(name = "updated", email = "master@example.com")
        ReflectionTestUtils.setField(updated, "id", 1L)
        every { userService.updateUser(1L, "updated", null) } returns updated

        mockMvc
            .perform(
                put("/api/v1/users/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {"name":"updated"}
                        """.trimIndent(),
                    ),
            ).andExpect(status().isOk)
    }

    @Test
    fun `should delete user`() {
        every { userService.deleteUser(1L) } returns Unit

        mockMvc
            .perform(delete("/api/v1/users/1"))
            .andExpect(status().isNoContent)
    }
}
