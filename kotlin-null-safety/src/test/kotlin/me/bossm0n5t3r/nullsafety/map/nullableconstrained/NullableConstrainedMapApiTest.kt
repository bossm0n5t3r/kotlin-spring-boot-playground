package me.bossm0n5t3r.nullsafety.map.nullableconstrained

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
import tools.jackson.module.kotlin.jacksonObjectMapper

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class NullableConstrainedMapApiTest {
    @LocalServerPort private var port: Int = 0
    private val client = HttpClient.newHttpClient()
    private val mapper = jacksonObjectMapper()

    @ParameterizedTest(name = "{0}, input={1}")
    @MethodSource("matrix")
    fun `map and entry validation follows each independent annotation`(case: Case, input: Input) {
        val response = post(case.path, input.json)
        val expected = case.expected(input)
        val label = "${case.path}, input=${input.label}"
        assertEquals(expected.status, response.statusCode(), label)
        expected.body?.let { body ->
            assertEquals(mapper.readTree(body), mapper.readTree(response.body()), label)
        }
        expected.fields?.let { fields ->
            assertEquals(
                mapper.readTree(mapper.writeValueAsString(ValidationErrorResponse(fields))),
                mapper.readTree(response.body()),
                label,
            )
        }
    }

    private fun post(path: String, json: String): HttpResponse<String> =
        client.send(
            HttpRequest.newBuilder(
                    URI("http://localhost:$port/api/null-safety/maps/nullable/not-null/$path"),
                )
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build(),
            HttpResponse.BodyHandlers.ofString(),
        )

    data class Expected(
        val status: Int,
        val body: String? = null,
        val fields: List<String>? = null,
    ) {
        companion object {
            fun accepted(body: String) = Expected(200, body = body)

            fun bindingFailure() = Expected(400)

            fun validationFailure(fields: List<String>) = Expected(400, fields = fields)
        }
    }

    data class Case(
        val path: String,
        val nullableValue: Boolean,
        val notNullValue: Boolean,
        val cascade: Boolean,
        val controllerValid: Boolean,
    ) {
        override fun toString(): String = path

        fun expected(input: Input): Expected =
            when (input.kind) {
                Kind.MISSING_MAP,
                Kind.NULL_MAP ->
                    if (controllerValid) Expected.validationFailure(listOf("children"))
                    else Expected.accepted(input.acceptedBody)
                Kind.EMPTY_MAP,
                Kind.VALID_ENTRIES -> Expected.accepted(input.acceptedBody)
                Kind.NULL_VALUE ->
                    when {
                        !nullableValue -> Expected.bindingFailure()
                        controllerValid && notNullValue ->
                            Expected.validationFailure(listOf("children[first]"))
                        else -> Expected.accepted(input.acceptedBody)
                    }
                Kind.MISSING_AGE,
                Kind.NULL_AGE,
                Kind.TWO_INVALID_ENTRIES ->
                    if (controllerValid && cascade) {
                        val fields =
                            if (input.kind == Kind.TWO_INVALID_ENTRIES) {
                                listOf("children[first].age", "children[second].age")
                            } else {
                                listOf("children[first].age")
                            }
                        Expected.validationFailure(fields)
                    } else {
                        Expected.accepted(input.acceptedBody)
                    }
                Kind.MIXED -> {
                    if (!nullableValue) {
                        Expected.bindingFailure()
                    } else {
                        val fields = buildList {
                            if (controllerValid && notNullValue) add("children[first]")
                            if (controllerValid && cascade) add("children[second].age")
                        }
                        if (fields.isEmpty()) Expected.accepted(input.acceptedBody)
                        else Expected.validationFailure(fields)
                    }
                }
            }
    }

    enum class Kind {
        MISSING_MAP,
        NULL_MAP,
        EMPTY_MAP,
        NULL_VALUE,
        MISSING_AGE,
        NULL_AGE,
        VALID_ENTRIES,
        TWO_INVALID_ENTRIES,
        MIXED,
    }

    data class Input(
        val label: String,
        val kind: Kind,
        val json: String,
        val acceptedBody: String,
    ) {
        override fun toString(): String = label
    }

    companion object {
        private val cases =
            listOf(
                Case("values/non-null/plain", false, false, false, false),
                Case("values/non-null/plain/valid", false, false, false, true),
                Case("values/non-null/cascade", false, false, true, false),
                Case("values/non-null/cascade/valid", false, false, true, true),
                Case("values/non-null/not-null", false, true, false, false),
                Case("values/non-null/not-null/valid", false, true, false, true),
                Case("values/non-null/not-null/cascade", false, true, true, false),
                Case("values/non-null/not-null/cascade/valid", false, true, true, true),
                Case("values/nullable/plain", true, false, false, false),
                Case("values/nullable/plain/valid", true, false, false, true),
                Case("values/nullable/cascade", true, false, true, false),
                Case("values/nullable/cascade/valid", true, false, true, true),
                Case("values/nullable/not-null", true, true, false, false),
                Case("values/nullable/not-null/valid", true, true, false, true),
                Case("values/nullable/not-null/cascade", true, true, true, false),
                Case("values/nullable/not-null/cascade/valid", true, true, true, true),
            )

        private val inputs =
            listOf(
                Input("missing children", Kind.MISSING_MAP, "{}", """{"children":null}"""),
                Input(
                    "null children",
                    Kind.NULL_MAP,
                    """{"children":null}""",
                    """{"children":null}""",
                ),
                Input(
                    "empty children",
                    Kind.EMPTY_MAP,
                    """{"children":{}}""",
                    """{"children":{}}""",
                ),
                Input(
                    "null first",
                    Kind.NULL_VALUE,
                    """{"children":{"first":null}}""",
                    """{"children":{"first":null}}""",
                ),
                Input(
                    "missing first age",
                    Kind.MISSING_AGE,
                    """{"children":{"first":{}}}""",
                    """{"children":{"first":{"age":null}}}""",
                ),
                Input(
                    "null first age",
                    Kind.NULL_AGE,
                    """{"children":{"first":{"age":null}}}""",
                    """{"children":{"first":{"age":null}}}""",
                ),
                Input(
                    "valid ages 0 and 25",
                    Kind.VALID_ENTRIES,
                    """{"children":{"first":{"age":0},"second":{"age":25}}}""",
                    """{"children":{"first":{"age":0},"second":{"age":25}}}""",
                ),
                Input(
                    "two invalid entries, reverse order",
                    Kind.TWO_INVALID_ENTRIES,
                    """{"children":{"second":{"age":null},"first":{}}}""",
                    """{"children":{"second":{"age":null},"first":{"age":null}}}""",
                ),
                Input(
                    "null first and invalid second",
                    Kind.MIXED,
                    """{"children":{"first":null,"second":{"age":null}}}""",
                    """{"children":{"first":null,"second":{"age":null}}}""",
                ),
            )

        @JvmStatic
        fun matrix(): Stream<Arguments> =
            cases.flatMap { case -> inputs.map { input -> Arguments.of(case, input) } }.stream()
    }
}
