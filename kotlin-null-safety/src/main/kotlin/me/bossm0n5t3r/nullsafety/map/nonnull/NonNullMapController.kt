package me.bossm0n5t3r.nullsafety.map.nonnull

import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(
    "/api/null-safety/maps/non-null/plain",
    consumes = [MediaType.APPLICATION_JSON_VALUE],
)
class NonNullMapController {
    // 1. Map nullable=X, Map @NotNull=X; value nullable=X, @NotNull=X, @Valid=X; Controller
    // @Valid=X.
    // Missing/null map or null value -> 400 (Jackson); missing/null age -> 200.
    // Empty map and valid entries age=0 or age=25 -> 200.
    @PostMapping("/values/non-null/plain")
    fun nonNullPlain(@RequestBody request: NonNullPlainValueRequest): NonNullPlainValueRequest =
        request

    // 2. Map nullable=X, Map @NotNull=X; value nullable=X, @NotNull=X, @Valid=X; Controller
    // @Valid=O.
    // Missing/null map or null value -> 400 (Jackson); missing/null age -> 200.
    // Empty map and valid entries age=0 or age=25 -> 200.
    @PostMapping("/values/non-null/plain/valid")
    fun nonNullPlainValid(
        @Valid @RequestBody request: NonNullPlainValueRequest,
    ): NonNullPlainValueRequest = request

    // 3. Map nullable=X, Map @NotNull=X; value nullable=X, @NotNull=X, @Valid=O; Controller
    // @Valid=X.
    // Missing/null map or null value -> 400 (Jackson); missing/null age -> 200.
    // Empty map and valid entries age=0 or age=25 -> 200.
    @PostMapping("/values/non-null/cascade")
    fun nonNullCascade(
        @RequestBody request: NonNullCascadeValueRequest,
    ): NonNullCascadeValueRequest = request

    // 4. Map nullable=X, Map @NotNull=X; value nullable=X, @NotNull=X, @Valid=O; Controller
    // @Valid=O.
    // Missing/null map or null value -> 400 (Jackson); missing/null age -> 400 (cascade).
    // Empty map and valid entries age=0 or age=25 -> 200.
    @PostMapping("/values/non-null/cascade/valid")
    fun nonNullCascadeValid(
        @Valid @RequestBody request: NonNullCascadeValueRequest,
    ): NonNullCascadeValueRequest = request

    // 5. Map nullable=X, Map @NotNull=X; value nullable=X, @NotNull=O, @Valid=X; Controller
    // @Valid=X.
    // Missing/null map or null value -> 400 (Jackson); missing/null age -> 200.
    // Empty map and valid entries age=0 or age=25 -> 200.
    @PostMapping("/values/non-null/not-null")
    fun nonNullNotNull(
        @RequestBody request: NonNullNotNullValueRequest,
    ): NonNullNotNullValueRequest = request

    // 6. Map nullable=X, Map @NotNull=X; value nullable=X, @NotNull=O, @Valid=X; Controller
    // @Valid=O.
    // Missing/null map or null value -> 400 (Jackson); missing/null age -> 200.
    // Empty map and valid entries age=0 or age=25 -> 200.
    @PostMapping("/values/non-null/not-null/valid")
    fun nonNullNotNullValid(
        @Valid @RequestBody request: NonNullNotNullValueRequest,
    ): NonNullNotNullValueRequest = request

    // 7. Map nullable=X, Map @NotNull=X; value nullable=X, @NotNull=O, @Valid=O; Controller
    // @Valid=X.
    // Missing/null map or null value -> 400 (Jackson); missing/null age -> 200.
    // Empty map and valid entries age=0 or age=25 -> 200.
    @PostMapping("/values/non-null/not-null/cascade")
    fun nonNullNotNullCascade(
        @RequestBody request: NonNullNotNullCascadeValueRequest,
    ): NonNullNotNullCascadeValueRequest = request

    // 8. Map nullable=X, Map @NotNull=X; value nullable=X, @NotNull=O, @Valid=O; Controller
    // @Valid=O.
    // Missing/null map or null value -> 400 (Jackson); missing/null age -> 400 (cascade).
    // Empty map and valid entries age=0 or age=25 -> 200.
    @PostMapping("/values/non-null/not-null/cascade/valid")
    fun nonNullNotNullCascadeValid(
        @Valid @RequestBody request: NonNullNotNullCascadeValueRequest,
    ): NonNullNotNullCascadeValueRequest = request

    // 9. Map nullable=X, Map @NotNull=X; value nullable=O, @NotNull=X, @Valid=X; Controller
    // @Valid=X.
    // Missing/null map -> 400 (Jackson); null value or missing/null age -> 200.
    // Empty map and valid entries age=0 or age=25 -> 200.
    @PostMapping("/values/nullable/plain")
    fun nullablePlain(@RequestBody request: NullablePlainValueRequest): NullablePlainValueRequest =
        request

    // 10. Map nullable=X, Map @NotNull=X; value nullable=O, @NotNull=X, @Valid=X; Controller
    // @Valid=O.
    // Missing/null map -> 400 (Jackson); null value or missing/null age -> 200.
    // Empty map and valid entries age=0 or age=25 -> 200.
    @PostMapping("/values/nullable/plain/valid")
    fun nullablePlainValid(
        @Valid @RequestBody request: NullablePlainValueRequest,
    ): NullablePlainValueRequest = request

    // 11. Map nullable=X, Map @NotNull=X; value nullable=O, @NotNull=X, @Valid=O; Controller
    // @Valid=X.
    // Missing/null map -> 400 (Jackson); null value or missing/null age -> 200.
    // Empty map and valid entries age=0 or age=25 -> 200.
    @PostMapping("/values/nullable/cascade")
    fun nullableCascade(
        @RequestBody request: NullableCascadeValueRequest,
    ): NullableCascadeValueRequest = request

    // 12. Map nullable=X, Map @NotNull=X; value nullable=O, @NotNull=X, @Valid=O; Controller
    // @Valid=O.
    // Missing/null map -> 400 (Jackson); null value -> 200; missing/null age -> 400 (cascade).
    // Empty map and valid entries age=0 or age=25 -> 200.
    @PostMapping("/values/nullable/cascade/valid")
    fun nullableCascadeValid(
        @Valid @RequestBody request: NullableCascadeValueRequest,
    ): NullableCascadeValueRequest = request

    // 13. Map nullable=X, Map @NotNull=X; value nullable=O, @NotNull=O, @Valid=X; Controller
    // @Valid=X.
    // Missing/null map -> 400 (Jackson); null value or missing/null age -> 200.
    // Empty map and valid entries age=0 or age=25 -> 200.
    @PostMapping("/values/nullable/not-null")
    fun nullableNotNull(
        @RequestBody request: NullableNotNullValueRequest,
    ): NullableNotNullValueRequest = request

    // 14. Map nullable=X, Map @NotNull=X; value nullable=O, @NotNull=O, @Valid=X; Controller
    // @Valid=O.
    // Missing/null map -> 400 (Jackson); null value -> 400 (Bean Validation).
    // Missing/null age, empty map and valid entries age=0 or age=25 -> 200.
    @PostMapping("/values/nullable/not-null/valid")
    fun nullableNotNullValid(
        @Valid @RequestBody request: NullableNotNullValueRequest,
    ): NullableNotNullValueRequest = request

    // 15. Map nullable=X, Map @NotNull=X; value nullable=O, @NotNull=O, @Valid=O; Controller
    // @Valid=X.
    // Missing/null map -> 400 (Jackson); null value or missing/null age -> 200.
    // Empty map and valid entries age=0 or age=25 -> 200.
    @PostMapping("/values/nullable/not-null/cascade")
    fun nullableNotNullCascade(
        @RequestBody request: NullableNotNullCascadeValueRequest,
    ): NullableNotNullCascadeValueRequest = request

    // 16. Map nullable=X, Map @NotNull=X; value nullable=O, @NotNull=O, @Valid=O; Controller
    // @Valid=O.
    // Missing/null map -> 400 (Jackson); null value -> 400 (Bean Validation).
    // Missing/null age -> 400 (cascade); empty map and valid entries age=0 or age=25 -> 200.
    @PostMapping("/values/nullable/not-null/cascade/valid")
    fun nullableNotNullCascadeValid(
        @Valid @RequestBody request: NullableNotNullCascadeValueRequest,
    ): NullableNotNullCascadeValueRequest = request
}
