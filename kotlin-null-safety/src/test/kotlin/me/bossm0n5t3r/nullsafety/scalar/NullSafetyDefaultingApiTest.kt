package me.bossm0n5t3r.nullsafety.scalar

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.stream.Stream
import kotlin.test.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import tools.jackson.module.kotlin.jacksonObjectMapper

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("primitive-defaulting")
class NullSafetyDefaultingApiTest {
    @LocalServerPort private var port: Int = 0
    private val client = HttpClient.newHttpClient()
    private val mapper = jacksonObjectMapper()

    @ParameterizedTest(name = "{0}, input={1}")
    @MethodSource("absentAgeCases")
    fun `defaulting converts primitive absence to zero but nullable validation still rejects it`(
        path: String,
        json: String,
    ) {
        val response = post(path, json)
        if (path == "nullable/not-null/valid") {
            assertEquals(400, response.statusCode())
            assertEquals(
                mapper.readTree("{\"fields\":[\"age\"]}"),
                mapper.readTree(response.body()),
            )
        } else {
            assertEquals(200, response.statusCode())
            val expected = if (path.startsWith("non-null/")) "{\"age\":0}" else "{\"age\":null}"
            assertEquals(mapper.readTree(expected), mapper.readTree(response.body()))
        }
    }

    @ParameterizedTest(name = "{0}, age={1}")
    @MethodSource("presentAgeCases")
    fun `defaulting preserves explicit zero and positive age`(path: String, age: Int) {
        val response = post(path, "{\"age\":$age}")
        assertEquals(200, response.statusCode())
        assertEquals(mapper.readTree("{\"age\":$age}"), mapper.readTree(response.body()))
    }

    private fun post(path: String, json: String): HttpResponse<String> =
        client.send(
            HttpRequest.newBuilder(
                    URI("http://localhost:$port/api/null-safety/ages/$path"),
                )
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build(),
            HttpResponse.BodyHandlers.ofString(),
        )

    companion object {
        private val paths =
            listOf(
                "non-null/plain",
                "non-null/plain/valid",
                "non-null/not-null",
                "non-null/not-null/valid",
                "nullable/plain",
                "nullable/plain/valid",
                "nullable/not-null",
                "nullable/not-null/valid",
            )

        @JvmStatic
        fun absentAgeCases(): Stream<Arguments> =
            paths
                .flatMap { path ->
                    listOf("{}", "{\"age\":null}").map { json -> Arguments.of(path, json) }
                }
                .stream()

        @JvmStatic
        fun presentAgeCases(): Stream<Arguments> =
            paths.flatMap { path -> listOf(0, 25).map { age -> Arguments.of(path, age) } }.stream()
    }
}
