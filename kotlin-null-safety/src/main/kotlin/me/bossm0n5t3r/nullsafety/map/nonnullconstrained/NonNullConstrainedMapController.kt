package me.bossm0n5t3r.nullsafety.map.nonnullconstrained

import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(
    "/api/null-safety/maps/non-null/not-null",
    consumes = [MediaType.APPLICATION_JSON_VALUE],
)
class NonNullConstrainedMapController {
    // Every case: absent/null map -> 400 (Jackson); empty/valid map (age=0 or age=25) -> 200.
    // Map nullable=X, map @NotNull=O in all cases; @field:NotNull is redundant for binding.

    // 1: map nullable=X, map @NotNull=O; value nullable=X, @NotNull=X, @Valid=X; controller
    // @Valid=X.
    // Null value -> 400 (Jackson); missing/null age -> 200.
    @PostMapping("/values/non-null/plain")
    fun nonNullPlain(@RequestBody request: NonNullPlainValueRequest): NonNullPlainValueRequest =
        request

    // 2: map nullable=X, map @NotNull=O; value nullable=X, @NotNull=X, @Valid=X; controller
    // @Valid=O.
    // Null value -> 400 (Jackson); missing/null age -> 200.
    @PostMapping("/values/non-null/plain/valid")
    fun nonNullPlainValid(
        @Valid @RequestBody request: NonNullPlainValueRequest,
    ): NonNullPlainValueRequest = request

    // 3: map nullable=X, map @NotNull=O; value nullable=X, @NotNull=X, @Valid=O; controller
    // @Valid=X.
    // Null value -> 400 (Jackson); missing/null age -> 200.
    @PostMapping("/values/non-null/cascade")
    fun nonNullCascade(
        @RequestBody request: NonNullCascadeValueRequest,
    ): NonNullCascadeValueRequest = request

    // 4: map nullable=X, map @NotNull=O; value nullable=X, @NotNull=X, @Valid=O; controller
    // @Valid=O.
    // Null value -> 400 (Jackson); missing/null age -> 400 (Bean Validation).
    @PostMapping("/values/non-null/cascade/valid")
    fun nonNullCascadeValid(
        @Valid @RequestBody request: NonNullCascadeValueRequest,
    ): NonNullCascadeValueRequest = request

    // 5: map nullable=X, map @NotNull=O; value nullable=X, @NotNull=O, @Valid=X; controller
    // @Valid=X.
    // Null value -> 400 (Jackson); missing/null age -> 200.
    @PostMapping("/values/non-null/not-null")
    fun nonNullNotNull(
        @RequestBody request: NonNullNotNullValueRequest,
    ): NonNullNotNullValueRequest = request

    // 6: map nullable=X, map @NotNull=O; value nullable=X, @NotNull=O, @Valid=X; controller
    // @Valid=O.
    // Null value -> 400 (Jackson); missing/null age -> 200.
    @PostMapping("/values/non-null/not-null/valid")
    fun nonNullNotNullValid(
        @Valid @RequestBody request: NonNullNotNullValueRequest,
    ): NonNullNotNullValueRequest = request

    // 7: map nullable=X, map @NotNull=O; value nullable=X, @NotNull=O, @Valid=O; controller
    // @Valid=X.
    // Null value -> 400 (Jackson); missing/null age -> 200.
    @PostMapping("/values/non-null/not-null/cascade")
    fun nonNullNotNullCascade(
        @RequestBody request: NonNullNotNullCascadeValueRequest,
    ): NonNullNotNullCascadeValueRequest = request

    // 8: map nullable=X, map @NotNull=O; value nullable=X, @NotNull=O, @Valid=O; controller
    // @Valid=O.
    // Null value -> 400 (Jackson); missing/null age -> 400 (Bean Validation).
    @PostMapping("/values/non-null/not-null/cascade/valid")
    fun nonNullNotNullCascadeValid(
        @Valid @RequestBody request: NonNullNotNullCascadeValueRequest,
    ): NonNullNotNullCascadeValueRequest = request

    // 9: map nullable=X, map @NotNull=O; value nullable=O, @NotNull=X, @Valid=X; controller
    // @Valid=X.
    // Null value -> 200; missing/null age -> 200.
    @PostMapping("/values/nullable/plain")
    fun nullablePlain(@RequestBody request: NullablePlainValueRequest): NullablePlainValueRequest =
        request

    // 10: map nullable=X, map @NotNull=O; value nullable=O, @NotNull=X, @Valid=X; controller
    // @Valid=O.
    // Null value -> 200; missing/null age -> 200.
    @PostMapping("/values/nullable/plain/valid")
    fun nullablePlainValid(
        @Valid @RequestBody request: NullablePlainValueRequest,
    ): NullablePlainValueRequest = request

    // 11: map nullable=X, map @NotNull=O; value nullable=O, @NotNull=X, @Valid=O; controller
    // @Valid=X.
    // Null value -> 200; missing/null age -> 200.
    @PostMapping("/values/nullable/cascade")
    fun nullableCascade(
        @RequestBody request: NullableCascadeValueRequest,
    ): NullableCascadeValueRequest = request

    // 12: map nullable=X, map @NotNull=O; value nullable=O, @NotNull=X, @Valid=O; controller
    // @Valid=O.
    // Null value -> 200; missing/null age -> 400 (Bean Validation).
    @PostMapping("/values/nullable/cascade/valid")
    fun nullableCascadeValid(
        @Valid @RequestBody request: NullableCascadeValueRequest,
    ): NullableCascadeValueRequest = request

    // 13: map nullable=X, map @NotNull=O; value nullable=O, @NotNull=O, @Valid=X; controller
    // @Valid=X.
    // Null value -> 200; missing/null age -> 200.
    @PostMapping("/values/nullable/not-null")
    fun nullableNotNull(
        @RequestBody request: NullableNotNullValueRequest,
    ): NullableNotNullValueRequest = request

    // 14: map nullable=X, map @NotNull=O; value nullable=O, @NotNull=O, @Valid=X; controller
    // @Valid=O.
    // Null value -> 400 (Bean Validation); missing/null age -> 200.
    @PostMapping("/values/nullable/not-null/valid")
    fun nullableNotNullValid(
        @Valid @RequestBody request: NullableNotNullValueRequest,
    ): NullableNotNullValueRequest = request

    // 15: map nullable=X, map @NotNull=O; value nullable=O, @NotNull=O, @Valid=O; controller
    // @Valid=X.
    // Null value -> 200; missing/null age -> 200.
    @PostMapping("/values/nullable/not-null/cascade")
    fun nullableNotNullCascade(
        @RequestBody request: NullableNotNullCascadeValueRequest,
    ): NullableNotNullCascadeValueRequest = request

    // 16: map nullable=X, map @NotNull=O; value nullable=O, @NotNull=O, @Valid=O; controller
    // @Valid=O.
    // Null value -> 400 (Bean Validation); missing/null age -> 400 (Bean Validation).
    @PostMapping("/values/nullable/not-null/cascade/valid")
    fun nullableNotNullCascadeValid(
        @Valid @RequestBody request: NullableNotNullCascadeValueRequest,
    ): NullableNotNullCascadeValueRequest = request
}
