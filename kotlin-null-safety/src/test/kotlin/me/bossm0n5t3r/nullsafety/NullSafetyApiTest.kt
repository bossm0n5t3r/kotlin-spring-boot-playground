package me.bossm0n5t3r.nullsafety

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
import org.springframework.http.HttpStatus
import tools.jackson.module.kotlin.jacksonObjectMapper

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class NullSafetyApiTest {
    @LocalServerPort private var port: Int = 0
    private val client = HttpClient.newHttpClient()
    private val mapper = jacksonObjectMapper()

    @ParameterizedTest(name = "{0}, input={1}, status={2}")
    @MethodSource("absentAgeCases")
    fun `missing and null age follow the nullability and validation matrix`(
        path: String,
        json: String,
        expectedStatus: Int,
        beanValidationFailure: Boolean,
    ) {
        val response = post(path, json)
        assertEquals(expectedStatus, response.statusCode())
        when {
            expectedStatus == 200 ->
                assertEquals(mapper.readTree("{\"age\":null}"), mapper.readTree(response.body()))
            beanValidationFailure ->
                assertEquals(
                    mapper.readTree("{\"fields\":[\"age\"]}"),
                    mapper.readTree(response.body()),
                )
        }
    }

    @ParameterizedTest(name = "{0}, age={1}")
    @MethodSource("presentAgeCases")
    fun `explicit zero and positive age are accepted by every combination`(path: String, age: Int) {
        val response = post(path, "{\"age\":$age}")
        assertEquals(200, response.statusCode())
        assertEquals(mapper.readTree("{\"age\":$age}"), mapper.readTree(response.body()))
    }

    private fun post(path: String, json: String): HttpResponse<String> =
        client.send(
            HttpRequest.newBuilder(URI("http://localhost:$port/api/null-safety/ages/$path"))
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
                    val beanValidationFailure = path == "nullable/not-null/valid"
                    val status =
                        if (path.startsWith("non-null/") || beanValidationFailure)
                            HttpStatus.BAD_REQUEST.value()
                        else HttpStatus.OK.value()
                    listOf("{}", "{\"age\":null}").map { json ->
                        Arguments.of(path, json, status, beanValidationFailure)
                    }
                }
                .stream()

        @JvmStatic
        fun presentAgeCases(): Stream<Arguments> =
            paths.flatMap { path -> listOf(0, 25).map { age -> Arguments.of(path, age) } }.stream()
    }
}
