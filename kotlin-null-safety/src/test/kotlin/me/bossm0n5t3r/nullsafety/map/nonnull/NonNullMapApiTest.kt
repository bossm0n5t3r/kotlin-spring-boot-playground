package me.bossm0n5t3r.nullsafety.map.nonnull

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.stream.Stream
import kotlin.test.assertEquals
import me.bossm0n5t3r.nullsafety.validation.ValidationErrorResponse
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import tools.jackson.module.kotlin.jacksonObjectMapper

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class NonNullMapApiTest {
    @LocalServerPort private var port: Int = 0
    private val client = HttpClient.newHttpClient()
    private val mapper = jacksonObjectMapper()

    @ParameterizedTest(name = "{0}, input={1}")
    @MethodSource("cases")
    fun `each map and value combination follows binding and validation semantics`(
        api: ApiCase,
        input: Input,
    ) {
        val response = post(api.path, input.json)
        val jacksonFailure =
            when (input) {
                Input.MISSING_MAP,
                Input.NULL_MAP -> true
                Input.NULL_VALUE,
                Input.MIXED_INVALID -> !api.valueNullable
                else -> false
            }
        val fields =
            when (input) {
                Input.NULL_VALUE ->
                    if (api.controllerValid && api.valueNotNull) listOf("children[first]")
                    else emptyList()
                Input.MISSING_AGE,
                Input.NULL_AGE ->
                    if (api.controllerValid && api.cascade) listOf("children[first].age")
                    else emptyList()
                Input.TWO_INVALID ->
                    if (api.controllerValid && api.cascade) {
                        listOf("children[first].age", "children[second].age")
                    } else {
                        emptyList()
                    }
                Input.MIXED_INVALID ->
                    buildList {
                        if (api.controllerValid && api.valueNotNull) add("children[first]")
                        if (api.controllerValid && api.cascade) add("children[second].age")
                    }
                else -> emptyList()
            }

        when {
            jacksonFailure ->
                assertEquals(
                    HttpStatus.BAD_REQUEST.value(),
                    response.statusCode(),
                    "${api.path}, $input",
                )
            fields.isNotEmpty() -> {
                assertEquals(
                    HttpStatus.BAD_REQUEST.value(),
                    response.statusCode(),
                    "${api.path}, $input",
                )
                assertEquals(
                    mapper.readTree(mapper.writeValueAsString(ValidationErrorResponse(fields))),
                    mapper.readTree(response.body()),
                    "${api.path}, $input",
                )
            }
            else -> {
                assertEquals(HttpStatus.OK.value(), response.statusCode(), "${api.path}, $input")
                assertEquals(
                    mapper.readTree(input.acceptedJson),
                    mapper.readTree(response.body()),
                    "${api.path}, $input",
                )
            }
        }
    }

    private fun post(path: String, json: String): HttpResponse<String> =
        client.send(
            HttpRequest.newBuilder(
                    URI("http://localhost:$port/api/null-safety/maps/non-null/plain$path"),
                )
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build(),
            HttpResponse.BodyHandlers.ofString(),
        )

    data class ApiCase(
        val path: String,
        val valueNullable: Boolean,
        val valueNotNull: Boolean,
        val cascade: Boolean,
        val controllerValid: Boolean,
    ) {
        override fun toString(): String = path
    }

    enum class Input(val json: String, val acceptedJson: String = json) {
        MISSING_MAP("{}", """{"children":null}"""),
        NULL_MAP("""{"children":null}"""),
        EMPTY_MAP("""{"children":{}}"""),
        NULL_VALUE("""{"children":{"first":null}}"""),
        MISSING_AGE("""{"children":{"first":{}}}""", """{"children":{"first":{"age":null}}}"""),
        NULL_AGE("""{"children":{"first":{"age":null}}}"""),
        VALID_ENTRIES("""{"children":{"first":{"age":0},"second":{"age":25}}}"""),
        TWO_INVALID(
            """{"children":{"second":{"age":null},"first":{}}}""",
            """{"children":{"second":{"age":null},"first":{"age":null}}}""",
        ),
        MIXED_INVALID("""{"children":{"first":null,"second":{"age":null}}}"""),
    }

    companion object {
        private val apiCases =
            listOf(
                ApiCase("/values/non-null/plain", false, false, false, false),
                ApiCase("/values/non-null/plain/valid", false, false, false, true),
                ApiCase("/values/non-null/cascade", false, false, true, false),
                ApiCase("/values/non-null/cascade/valid", false, false, true, true),
                ApiCase("/values/non-null/not-null", false, true, false, false),
                ApiCase("/values/non-null/not-null/valid", false, true, false, true),
                ApiCase("/values/non-null/not-null/cascade", false, true, true, false),
                ApiCase("/values/non-null/not-null/cascade/valid", false, true, true, true),
                ApiCase("/values/nullable/plain", true, false, false, false),
                ApiCase("/values/nullable/plain/valid", true, false, false, true),
                ApiCase("/values/nullable/cascade", true, false, true, false),
                ApiCase("/values/nullable/cascade/valid", true, false, true, true),
                ApiCase("/values/nullable/not-null", true, true, false, false),
                ApiCase("/values/nullable/not-null/valid", true, true, false, true),
                ApiCase("/values/nullable/not-null/cascade", true, true, true, false),
                ApiCase("/values/nullable/not-null/cascade/valid", true, true, true, true),
            )

        @JvmStatic
        fun cases(): Stream<Arguments> =
            apiCases
                .flatMap { api -> Input.entries.map { input -> Arguments.of(api, input) } }
                .stream()
    }
}
