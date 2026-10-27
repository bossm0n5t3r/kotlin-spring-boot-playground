package me.bossm0n5t3r.nullsafety.map.nonnullconstrained

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
class NonNullConstrainedMapApiTest {
    @LocalServerPort private var port: Int = 0
    private val client = HttpClient.newHttpClient()
    private val mapper = jacksonObjectMapper()

    @ParameterizedTest(name = "{0}, input={1}")
    @MethodSource("cases")
    fun `map value and validation combinations over HTTP`(endpoint: Endpoint, input: Input) {
        val response = post(endpoint.path, input.json)
        val jacksonFailure =
            input == Input.MISSING_MAP ||
                input == Input.NULL_MAP ||
                (!endpoint.valueNullable && (input == Input.NULL_VALUE || input == Input.MIXED))
        val ageFields =
            if (endpoint.controllerValid && endpoint.valueCascade) {
                when (input) {
                    Input.MISSING_AGE,
                    Input.NULL_AGE -> listOf("children[first].age")
                    Input.TWO_INVALID -> listOf("children[first].age", "children[second].age")
                    Input.MIXED -> listOf("children[second].age")
                    else -> emptyList()
                }
            } else {
                emptyList()
            }
        val nullValueFields =
            if (
                endpoint.controllerValid &&
                    endpoint.valueNullable &&
                    endpoint.valueNotNull &&
                    (input == Input.NULL_VALUE || input == Input.MIXED)
            ) {
                listOf("children[first]")
            } else {
                emptyList()
            }
        val fields = (nullValueFields + ageFields).sorted()
        val label = "${endpoint.path}, input=$input"
        assertEquals(
            if (jacksonFailure || fields.isNotEmpty()) HttpStatus.BAD_REQUEST.value()
            else HttpStatus.OK.value(),
            response.statusCode(),
            label,
        )
        when {
            jacksonFailure -> Unit // Jackson binding errors have no stable response body.
            fields.isNotEmpty() ->
                assertEquals(
                    mapper.readTree(mapper.writeValueAsString(ValidationErrorResponse(fields))),
                    mapper.readTree(response.body()),
                    label,
                )
            else ->
                assertEquals(
                    mapper.readTree(requireNotNull(input.acceptedJson)),
                    mapper.readTree(response.body()),
                    label,
                )
        }
    }

    private fun post(path: String, json: String): HttpResponse<String> =
        client.send(
            HttpRequest.newBuilder(
                    URI("http://localhost:$port/api/null-safety/maps/non-null/not-null$path"),
                )
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build(),
            HttpResponse.BodyHandlers.ofString(),
        )

    data class Endpoint(
        val path: String,
        val valueNullable: Boolean,
        val valueNotNull: Boolean,
        val valueCascade: Boolean,
        val controllerValid: Boolean,
    ) {
        override fun toString(): String = path
    }

    enum class Input(val json: String, val acceptedJson: String? = json) {
        MISSING_MAP("{}", null),
        NULL_MAP("""{"children":null}""", null),
        EMPTY_MAP("""{"children":{}}"""),
        NULL_VALUE("""{"children":{"first":null}}"""),
        MISSING_AGE("""{"children":{"first":{}}}""", """{"children":{"first":{"age":null}}}"""),
        NULL_AGE("""{"children":{"first":{"age":null}}}"""),
        VALID("""{"children":{"first":{"age":0},"second":{"age":25}}}"""),
        TWO_INVALID(
            """{"children":{"second":{"age":null},"first":{}}}""",
            """{"children":{"second":{"age":null},"first":{"age":null}}}""",
        ),
        MIXED("""{"children":{"first":null,"second":{"age":null}}}"""),
    }

    companion object {
        private val endpoints =
            listOf(
                Endpoint("/values/non-null/plain", false, false, false, false),
                Endpoint("/values/non-null/plain/valid", false, false, false, true),
                Endpoint("/values/non-null/cascade", false, false, true, false),
                Endpoint("/values/non-null/cascade/valid", false, false, true, true),
                Endpoint("/values/non-null/not-null", false, true, false, false),
                Endpoint("/values/non-null/not-null/valid", false, true, false, true),
                Endpoint("/values/non-null/not-null/cascade", false, true, true, false),
                Endpoint("/values/non-null/not-null/cascade/valid", false, true, true, true),
                Endpoint("/values/nullable/plain", true, false, false, false),
                Endpoint("/values/nullable/plain/valid", true, false, false, true),
                Endpoint("/values/nullable/cascade", true, false, true, false),
                Endpoint("/values/nullable/cascade/valid", true, false, true, true),
                Endpoint("/values/nullable/not-null", true, true, false, false),
                Endpoint("/values/nullable/not-null/valid", true, true, false, true),
                Endpoint("/values/nullable/not-null/cascade", true, true, true, false),
                Endpoint("/values/nullable/not-null/cascade/valid", true, true, true, true),
            )

        @JvmStatic
        fun cases(): Stream<Arguments> =
            endpoints
                .flatMap { endpoint ->
                    Input.entries.map { input -> Arguments.of(endpoint, input) }
                }
                .stream()
    }
}
