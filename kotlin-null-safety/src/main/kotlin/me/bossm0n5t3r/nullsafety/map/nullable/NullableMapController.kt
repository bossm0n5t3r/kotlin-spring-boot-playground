package me.bossm0n5t3r.nullsafety.map.nullable

import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(
    "/api/null-safety/maps/nullable/plain",
    consumes = [MediaType.APPLICATION_JSON_VALUE],
)
class NullableMapController {
    // Map nullable=O, Map @NotNull=X; value nullable=X, @NotNull=X, @Valid=X; Controller @Valid=X.
    // Missing/null map -> 200; null value -> 400 (Jackson); missing/null age -> 200; empty/valid
    // map -> 200.
    @PostMapping("/values/non-null/plain")
    fun nonNullPlain(@RequestBody request: NonNullPlainValueRequest): NonNullPlainValueRequest =
        request

    // Map nullable=O, Map @NotNull=X; value nullable=X, @NotNull=X, @Valid=X; Controller @Valid=O.
    // Missing/null map -> 200; null value -> 400 (Jackson); missing/null age -> 200; empty/valid
    // map -> 200.
    @PostMapping("/values/non-null/plain/valid")
    fun nonNullPlainValid(
        @Valid @RequestBody request: NonNullPlainValueRequest,
    ): NonNullPlainValueRequest = request

    // Map nullable=O, Map @NotNull=X; value nullable=X, @NotNull=X, @Valid=O; Controller @Valid=X.
    // Missing/null map -> 200; null value -> 400 (Jackson); missing/null age -> 200; empty/valid
    // map -> 200.
    @PostMapping("/values/non-null/cascade")
    fun nonNullCascade(
        @RequestBody request: NonNullCascadeValueRequest,
    ): NonNullCascadeValueRequest = request

    // Map nullable=O, Map @NotNull=X; value nullable=X, @NotNull=X, @Valid=O; Controller @Valid=O.
    // Missing/null map -> 200; null value -> 400 (Jackson); missing/null age -> 400 (Bean
    // Validation); empty/valid map -> 200.
    @PostMapping("/values/non-null/cascade/valid")
    fun nonNullCascadeValid(
        @Valid @RequestBody request: NonNullCascadeValueRequest,
    ): NonNullCascadeValueRequest = request

    // Map nullable=O, Map @NotNull=X; value nullable=X, @NotNull=O, @Valid=X; Controller @Valid=X.
    // Missing/null map -> 200; null value -> 400 (Jackson); missing/null age -> 200; empty/valid
    // map -> 200.
    @PostMapping("/values/non-null/not-null")
    fun nonNullNotNull(
        @RequestBody request: NonNullNotNullValueRequest,
    ): NonNullNotNullValueRequest = request

    // Map nullable=O, Map @NotNull=X; value nullable=X, @NotNull=O, @Valid=X; Controller @Valid=O.
    // Missing/null map -> 200; null value -> 400 (Jackson); missing/null age -> 200; empty/valid
    // map -> 200.
    @PostMapping("/values/non-null/not-null/valid")
    fun nonNullNotNullValid(
        @Valid @RequestBody request: NonNullNotNullValueRequest,
    ): NonNullNotNullValueRequest = request

    // Map nullable=O, Map @NotNull=X; value nullable=X, @NotNull=O, @Valid=O; Controller @Valid=X.
    // Missing/null map -> 200; null value -> 400 (Jackson); missing/null age -> 200; empty/valid
    // map -> 200.
    @PostMapping("/values/non-null/not-null/cascade")
    fun nonNullNotNullCascade(
        @RequestBody request: NonNullNotNullCascadeValueRequest,
    ): NonNullNotNullCascadeValueRequest = request

    // Map nullable=O, Map @NotNull=X; value nullable=X, @NotNull=O, @Valid=O; Controller @Valid=O.
    // Missing/null map -> 200; null value -> 400 (Jackson); missing/null age -> 400 (Bean
    // Validation); empty/valid map -> 200.
    @PostMapping("/values/non-null/not-null/cascade/valid")
    fun nonNullNotNullCascadeValid(
        @Valid @RequestBody request: NonNullNotNullCascadeValueRequest,
    ): NonNullNotNullCascadeValueRequest = request

    // Map nullable=O, Map @NotNull=X; value nullable=O, @NotNull=X, @Valid=X; Controller @Valid=X.
    // Missing/null map or null value -> 200; missing/null age -> 200; empty/valid map -> 200.
    @PostMapping("/values/nullable/plain")
    fun nullablePlain(@RequestBody request: NullablePlainValueRequest): NullablePlainValueRequest =
        request

    // Map nullable=O, Map @NotNull=X; value nullable=O, @NotNull=X, @Valid=X; Controller @Valid=O.
    // Missing/null map or null value -> 200; missing/null age -> 200; empty/valid map -> 200.
    @PostMapping("/values/nullable/plain/valid")
    fun nullablePlainValid(
        @Valid @RequestBody request: NullablePlainValueRequest,
    ): NullablePlainValueRequest = request

    // Map nullable=O, Map @NotNull=X; value nullable=O, @NotNull=X, @Valid=O; Controller @Valid=X.
    // Missing/null map or null value -> 200; missing/null age -> 200; empty/valid map -> 200.
    @PostMapping("/values/nullable/cascade")
    fun nullableCascade(
        @RequestBody request: NullableCascadeValueRequest,
    ): NullableCascadeValueRequest = request

    // Map nullable=O, Map @NotNull=X; value nullable=O, @NotNull=X, @Valid=O; Controller @Valid=O.
    // Missing/null map or null value -> 200; missing/null age -> 400 (Bean Validation); empty/valid
    // map -> 200.
    @PostMapping("/values/nullable/cascade/valid")
    fun nullableCascadeValid(
        @Valid @RequestBody request: NullableCascadeValueRequest,
    ): NullableCascadeValueRequest = request

    // Map nullable=O, Map @NotNull=X; value nullable=O, @NotNull=O, @Valid=X; Controller @Valid=X.
    // Missing/null map or null value -> 200; missing/null age -> 200; empty/valid map -> 200.
    @PostMapping("/values/nullable/not-null")
    fun nullableNotNull(
        @RequestBody request: NullableNotNullValueRequest,
    ): NullableNotNullValueRequest = request

    // Map nullable=O, Map @NotNull=X; value nullable=O, @NotNull=O, @Valid=X; Controller @Valid=O.
    // Missing/null map -> 200; null value -> 400 (Bean Validation); missing/null age -> 200;
    // empty/valid map -> 200.
    @PostMapping("/values/nullable/not-null/valid")
    fun nullableNotNullValid(
        @Valid @RequestBody request: NullableNotNullValueRequest,
    ): NullableNotNullValueRequest = request

    // Map nullable=O, Map @NotNull=X; value nullable=O, @NotNull=O, @Valid=O; Controller @Valid=X.
    // Missing/null map or null value -> 200; missing/null age -> 200; empty/valid map -> 200.
    @PostMapping("/values/nullable/not-null/cascade")
    fun nullableNotNullCascade(
        @RequestBody request: NullableNotNullCascadeValueRequest,
    ): NullableNotNullCascadeValueRequest = request

    // Map nullable=O, Map @NotNull=X; value nullable=O, @NotNull=O, @Valid=O; Controller @Valid=O.
    // Missing/null map -> 200; null value or missing/null age -> 400 (Bean Validation); empty/valid
    // map -> 200.
    @PostMapping("/values/nullable/not-null/cascade/valid")
    fun nullableNotNullCascadeValid(
        @Valid @RequestBody request: NullableNotNullCascadeValueRequest,
    ): NullableNotNullCascadeValueRequest = request
}
