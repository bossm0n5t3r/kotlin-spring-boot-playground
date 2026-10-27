package me.bossm0n5t3r.nullsafety.nested

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
class NestedNullSafetyApiTest {
    @LocalServerPort private var port: Int = 0
    private val client = HttpClient.newHttpClient()
    private val mapper = jacksonObjectMapper()

    @ParameterizedTest(name = "{0}, input={1}, status={2}")
    @MethodSource("absentChildCases")
    fun `missing and null child distinguish nullable required and non-null parents`(
        path: String,
        json: String,
        expectedStatus: Int,
        validationField: String?,
    ) {
        val response = post(path, json)
        assertEquals(expectedStatus, response.statusCode())
        when {
            expectedStatus == HttpStatus.OK.value() ->
                assertEquals(mapper.readTree("{\"child\":null}"), mapper.readTree(response.body()))
            validationField != null ->
                assertEquals(
                    mapper.readTree("{\"fields\":[\"$validationField\"]}"),
                    mapper.readTree(response.body()),
                )
        }
    }

    @ParameterizedTest(name = "{0}, input={1}, status={2}")
    @MethodSource("absentChildAgeCases")
    fun `missing and null nested age distinguish cascade from no cascade`(
        path: String,
        json: String,
        expectedStatus: Int,
    ) {
        val response = post(path, json)
        assertEquals(expectedStatus, response.statusCode())
        if (expectedStatus == HttpStatus.OK.value()) {
            assertEquals(
                mapper.readTree("{\"child\":{\"age\":null}}"),
                mapper.readTree(response.body()),
            )
        } else {
            assertEquals(
                mapper.readTree("{\"fields\":[\"child.age\"]}"),
                mapper.readTree(response.body()),
            )
        }
    }

    @ParameterizedTest(name = "{0}, age={1}")
    @MethodSource("presentChildAgeCases")
    fun `zero and positive nested age are echoed by every endpoint`(path: String, age: Int) {
        val response = post(path, "{\"child\":{\"age\":$age}}")
        assertEquals(HttpStatus.OK.value(), response.statusCode())
        assertEquals(
            mapper.readTree("{\"child\":{\"age\":$age}}"),
            mapper.readTree(response.body()),
        )
    }

    private fun post(path: String, json: String): HttpResponse<String> =
        client.send(
            HttpRequest.newBuilder(URI("http://localhost:$port/api/null-safety/nested/$path"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build(),
            HttpResponse.BodyHandlers.ofString(),
        )

    companion object {
        private data class NestedCase(
            val path: String,
            val absentChildStatus: Int,
            val childValidationField: String?,
            val absentAgeStatus: Int,
        )

        private val cases =
            listOf(
                NestedCase("non-null/plain", 400, null, 200),
                NestedCase("non-null/plain/valid", 400, null, 200),
                NestedCase("non-null/cascade", 400, null, 200),
                NestedCase("non-null/cascade/valid", 400, null, 400),
                NestedCase("non-null/not-null", 400, null, 200),
                NestedCase("non-null/not-null/valid", 400, null, 200),
                NestedCase("non-null/not-null/cascade", 400, null, 200),
                NestedCase("non-null/not-null/cascade/valid", 400, null, 400),
                NestedCase("nullable/plain", 200, null, 200),
                NestedCase("nullable/plain/valid", 200, null, 200),
                NestedCase("nullable/cascade", 200, null, 200),
                NestedCase("nullable/cascade/valid", 200, null, 400),
                NestedCase("nullable/not-null", 200, null, 200),
                NestedCase("nullable/not-null/valid", 400, "child", 200),
                NestedCase("nullable/not-null/cascade", 200, null, 200),
                NestedCase("nullable/not-null/cascade/valid", 400, "child", 400),
            )

        @JvmStatic
        fun absentChildCases(): Stream<Arguments> =
            cases
                .flatMap { case ->
                    listOf("{}", "{\"child\":null}").map { json ->
                        Arguments.of(
                            case.path,
                            json,
                            case.absentChildStatus,
                            case.childValidationField,
                        )
                    }
                }
                .stream()

        @JvmStatic
        fun absentChildAgeCases(): Stream<Arguments> =
            cases
                .flatMap { case ->
                    listOf("{\"child\":{}}", "{\"child\":{\"age\":null}}").map { json ->
                        Arguments.of(case.path, json, case.absentAgeStatus)
                    }
                }
                .stream()

        @JvmStatic
        fun presentChildAgeCases(): Stream<Arguments> =
            cases
                .flatMap { case -> listOf(0, 25).map { age -> Arguments.of(case.path, age) } }
                .stream()
    }
}
