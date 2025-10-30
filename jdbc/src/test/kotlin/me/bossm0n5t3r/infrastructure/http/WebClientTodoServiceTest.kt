package me.bossm0n5t3r.infrastructure.http

import kotlinx.coroutines.test.runTest
import me.bossm0n5t3r.application.WebClientTodoService
import me.bossm0n5t3r.infrastructure.http.dto.CreateTodoRequest
import me.bossm0n5t3r.infrastructure.http.dto.UpdateTodoRequest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class WebClientTodoServiceTest {
    companion object {
        @JvmStatic
        val server: MockWebServer = MockWebServer().apply { start() }

        @JvmStatic
        @AfterAll
        fun tearDown() {
            server.shutdown()
        }

        @JvmStatic
        @DynamicPropertySource
        fun registerProps(registry: DynamicPropertyRegistry) {
            registry.add("clients.jsonplaceholder.base-url") { server.url("/").toString().removeSuffix("/") }
        }
    }

    @Autowired
    private lateinit var todoService: WebClientTodoService

    @Test
    fun `getTodo should call external api with correct path and parse response`() =
        runTest {
            // given
            val body =
                """
                {
                  "userId": 1,
                  "id": 1,
                  "title": "delectus aut autem",
                  "completed": false
                }
                """.trimIndent()
            server.enqueue(MockResponse().setResponseCode(200).setBody(body).addHeader("Content-Type", "application/json"))

            // when
            val todo = todoService.getTodo(1)

            // then
            val recorded = server.takeRequest()
            assertThat(recorded.method).isEqualTo("GET")
            assertThat(recorded.path).isEqualTo("/todos/1")
            assertThat(todo.id).isEqualTo(1)
            assertThat(todo.userId).isEqualTo(1)
            assertThat(todo.title).isEqualTo("delectus aut autem")
            assertThat(todo.completed).isFalse()
        }

    @Test
    fun `create should POST to external api with body and parse response`() =
        runTest {
            // given
            val responseBody =
                """
                {
                  "userId": 99,
                  "id": 201,
                  "title": "new item",
                  "completed": true
                }
                """.trimIndent()
            server.enqueue(
                MockResponse()
                    .setResponseCode(201)
                    .setBody(responseBody)
                    .addHeader("Content-Type", "application/json"),
            )

            // when
            val created = todoService.create(CreateTodoRequest(userId = 99, title = "new item", completed = true))

            // then
            val recorded = server.takeRequest()
            assertThat(recorded.method).isEqualTo("POST")
            assertThat(recorded.path).isEqualTo("/todos")
            assertThat(recorded.getHeader("Content-Type")).contains("application/json")
            assertThat(recorded.body.readUtf8()).isEqualTo(
                "{" +
                    "\"userId\":99," +
                    "\"title\":\"new item\"," +
                    "\"completed\":true" +
                    "}",
            )
            assertThat(created.id).isEqualTo(201)
            assertThat(created.userId).isEqualTo(99)
            assertThat(created.title).isEqualTo("new item")
            assertThat(created.completed).isTrue()
        }

    @Test
    fun `update should PUT to external api with body and parse response`() =
        runTest {
            // given
            val responseBody =
                """
                {
                  "userId": 99,
                  "id": 201,
                  "title": "updated item",
                  "completed": false
                }
                """.trimIndent()
            server.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(responseBody)
                    .addHeader("Content-Type", "application/json"),
            )

            // when
            val updated = todoService.update(201, UpdateTodoRequest(title = "updated item", completed = false))

            // then
            val recorded = server.takeRequest()
            assertThat(recorded.method).isEqualTo("PUT")
            assertThat(recorded.path).isEqualTo("/todos/201")
            assertThat(recorded.getHeader("Content-Type")).contains("application/json")
            assertThat(recorded.body.readUtf8()).isEqualTo(
                "{" +
                    "\"title\":\"updated item\"," +
                    "\"completed\":false" +
                    "}",
            )
            assertThat(updated.id).isEqualTo(201)
            assertThat(updated.userId).isEqualTo(99)
            assertThat(updated.title).isEqualTo("updated item")
            assertThat(updated.completed).isFalse()
        }

    @Test
    fun `delete should send DELETE to external api`() =
        runTest {
            // given
            server.enqueue(
                MockResponse()
                    .setResponseCode(204),
            )

            // when
            todoService.delete(123)

            // then
            val recorded = server.takeRequest()
            assertThat(recorded.method).isEqualTo("DELETE")
            assertThat(recorded.path).isEqualTo("/todos/123")
        }
}
