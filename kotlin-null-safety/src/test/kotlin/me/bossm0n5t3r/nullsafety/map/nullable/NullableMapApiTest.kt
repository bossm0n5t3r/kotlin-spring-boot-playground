package me.bossm0n5t3r.nullsafety.map.nullable

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
class NullableMapApiTest {
    @LocalServerPort private var port: Int = 0
    private val client = HttpClient.newHttpClient()
    private val mapper = jacksonObjectMapper()

    data class Api(
        val path: String,
        val nullableValue: Boolean,
        val notNullValue: Boolean,
        val cascade: Boolean,
        val controllerValid: Boolean,
    ) {
        override fun toString(): String = path
    }

    enum class Input(val json: String, val acceptedBody: String) {
        MISSING_MAP("{}", """{"children":null}"""),
        NULL_MAP("""{"children":null}""", """{"children":null}"""),
        EMPTY_MAP("""{"children":{}}""", """{"children":{}}"""),
        NULL_VALUE("""{"children":{"first":null}}""", """{"children":{"first":null}}"""),
        MISSING_AGE(
            """{"children":{"first":{}}}""",
            """{"children":{"first":{"age":null}}}""",
        ),
        NULL_AGE(
            """{"children":{"first":{"age":null}}}""",
            """{"children":{"first":{"age":null}}}""",
        ),
        VALID_ENTRIES(
            """{"children":{"first":{"age":0},"second":{"age":25}}}""",
            """{"children":{"first":{"age":0},"second":{"age":25}}}""",
        ),
        TWO_INVALID_AGES(
            """{"children":{"second":{"age":null},"first":{}}}""",
            """{"children":{"second":{"age":null},"first":{"age":null}}}""",
        ),
        MIXED_NULL_VALUE_AND_AGE(
            """{"children":{"first":null,"second":{"age":null}}}""",
            """{"children":{"first":null,"second":{"age":null}}}""",
        ),
    }

    @ParameterizedTest(name = "{0}, input={1}")
    @MethodSource("cases")
    fun `each Map configuration handles all nine HTTP inputs`(api: Api, input: Input) {
        val response = post(api.path, input.json)
        val label = "${api.path}, input=$input"
        val jacksonFailure =
            !api.nullableValue &&
                (input == Input.NULL_VALUE || input == Input.MIXED_NULL_VALUE_AND_AGE)
        val fields = validationFields(api, input)

        when {
            jacksonFailure ->
                assertEquals(HttpStatus.BAD_REQUEST.value(), response.statusCode(), label)
            fields.isNotEmpty() -> {
                assertEquals(HttpStatus.BAD_REQUEST.value(), response.statusCode(), label)
                assertFields(response, fields, label)
            }
            else -> {
                assertEquals(HttpStatus.OK.value(), response.statusCode(), label)
                assertEquals(
                    mapper.readTree(input.acceptedBody),
                    mapper.readTree(response.body()),
                    label,
                )
            }
        }
    }

    private fun validationFields(api: Api, input: Input): List<String> {
        if (!api.controllerValid) return emptyList()
        return when (input) {
            Input.NULL_VALUE ->
                if (api.nullableValue && api.notNullValue) listOf("children[first]")
                else emptyList()
            Input.MISSING_AGE,
            Input.NULL_AGE -> if (api.cascade) listOf("children[first].age") else emptyList()
            Input.TWO_INVALID_AGES ->
                if (api.cascade) listOf("children[first].age", "children[second].age")
                else emptyList()
            Input.MIXED_NULL_VALUE_AND_AGE ->
                buildList {
                    if (api.notNullValue) add("children[first]")
                    if (api.cascade) add("children[second].age")
                }
            Input.MISSING_MAP,
            Input.NULL_MAP,
            Input.EMPTY_MAP,
            Input.VALID_ENTRIES -> emptyList()
        }
    }

    private fun assertFields(response: HttpResponse<String>, fields: List<String>, label: String) {
        assertEquals(
            mapper.readTree(mapper.writeValueAsString(ValidationErrorResponse(fields))),
            mapper.readTree(response.body()),
            label,
        )
    }

    private fun post(path: String, json: String): HttpResponse<String> =
        client.send(
            HttpRequest.newBuilder(
                    URI("http://localhost:$port/api/null-safety/maps/nullable/plain/$path"),
                )
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build(),
            HttpResponse.BodyHandlers.ofString(),
        )

    companion object {
        private val apis =
            listOf(
                    Api("values/non-null/plain", false, false, false, false),
                    Api("values/non-null/cascade", false, false, true, false),
                    Api("values/non-null/not-null", false, true, false, false),
                    Api("values/non-null/not-null/cascade", false, true, true, false),
                    Api("values/nullable/plain", true, false, false, false),
                    Api("values/nullable/cascade", true, false, true, false),
                    Api("values/nullable/not-null", true, true, false, false),
                    Api("values/nullable/not-null/cascade", true, true, true, false),
                )
                .flatMap { base ->
                    listOf(base, base.copy(path = "${base.path}/valid", controllerValid = true))
                }

        @JvmStatic
        fun cases(): Stream<Arguments> =
            apis.flatMap { api -> Input.entries.map { input -> Arguments.of(api, input) } }.stream()
    }
}
