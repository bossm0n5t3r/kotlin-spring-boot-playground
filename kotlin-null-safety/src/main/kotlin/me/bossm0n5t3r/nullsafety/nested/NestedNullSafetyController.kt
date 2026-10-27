package me.bossm0n5t3r.nullsafety.nested

import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/null-safety/nested", consumes = [MediaType.APPLICATION_JSON_VALUE])
class NestedNullSafetyController {
    // Case 1: child nullable=X, child @NotNull=X, child @Valid=X, Controller @Valid=X
    // child 누락/null -> 400 (Jackson), child.age 누락/null -> 200
    // child.age=0 또는 child.age=25 -> 200 OK
    @PostMapping("/non-null/plain")
    fun nonNullPlain(@RequestBody request: NonNullPlainNestedRequest): NonNullPlainNestedRequest =
        request

    // Case 2: child nullable=X, child @NotNull=X, child @Valid=X, Controller @Valid=O
    // child 누락/null -> 400 (Jackson), child.age 누락/null -> 200 (cascade X)
    // child.age=0 또는 child.age=25 -> 200 OK
    @PostMapping("/non-null/plain/valid")
    fun nonNullPlainValid(
        @Valid @RequestBody request: NonNullPlainNestedRequest,
    ): NonNullPlainNestedRequest = request

    // Case 3: child nullable=X, child @NotNull=X, child @Valid=O, Controller @Valid=X
    // child 누락/null -> 400 (Jackson), child.age 누락/null -> 200 (검증 미실행)
    // child.age=0 또는 child.age=25 -> 200 OK
    @PostMapping("/non-null/cascade")
    fun nonNullCascade(
        @RequestBody request: NonNullCascadeNestedRequest,
    ): NonNullCascadeNestedRequest = request

    // Case 4: child nullable=X, child @NotNull=X, child @Valid=O, Controller @Valid=O
    // child 누락/null -> 400 (Jackson), child.age 누락/null -> 400, fields=[child.age]
    // child.age=0 또는 child.age=25 -> 200 OK
    @PostMapping("/non-null/cascade/valid")
    fun nonNullCascadeValid(
        @Valid @RequestBody request: NonNullCascadeNestedRequest,
    ): NonNullCascadeNestedRequest = request

    // Case 5: child nullable=X, child @NotNull=O, child @Valid=X, Controller @Valid=X
    // child 누락/null -> 400 (Jackson), child.age 누락/null -> 200
    // child.age=0 또는 child.age=25 -> 200 OK
    @PostMapping("/non-null/not-null")
    fun nonNullNotNull(
        @RequestBody request: NonNullNotNullNestedRequest,
    ): NonNullNotNullNestedRequest = request

    // Case 6: child nullable=X, child @NotNull=O, child @Valid=X, Controller @Valid=O
    // child 누락/null -> 400 (Jackson), child.age 누락/null -> 200 (cascade X)
    // child.age=0 또는 child.age=25 -> 200 OK
    @PostMapping("/non-null/not-null/valid")
    fun nonNullNotNullValid(
        @Valid @RequestBody request: NonNullNotNullNestedRequest,
    ): NonNullNotNullNestedRequest = request

    // Case 7: child nullable=X, child @NotNull=O, child @Valid=O, Controller @Valid=X
    // child 누락/null -> 400 (Jackson), child.age 누락/null -> 200 (검증 미실행)
    // child.age=0 또는 child.age=25 -> 200 OK
    @PostMapping("/non-null/not-null/cascade")
    fun nonNullNotNullCascade(
        @RequestBody request: NonNullNotNullCascadeNestedRequest,
    ): NonNullNotNullCascadeNestedRequest = request

    // Case 8: child nullable=X, child @NotNull=O, child @Valid=O, Controller @Valid=O
    // child 누락/null -> 400 (Jackson), child.age 누락/null -> 400, fields=[child.age]
    // child.age=0 또는 child.age=25 -> 200 OK
    @PostMapping("/non-null/not-null/cascade/valid")
    fun nonNullNotNullCascadeValid(
        @Valid @RequestBody request: NonNullNotNullCascadeNestedRequest,
    ): NonNullNotNullCascadeNestedRequest = request

    // Case 9: child nullable=O, child @NotNull=X, child @Valid=X, Controller @Valid=X
    // child 누락/null -> 200, child.age 누락/null -> 200
    // child.age=0 또는 child.age=25 -> 200 OK
    @PostMapping("/nullable/plain")
    fun nullablePlain(
        @RequestBody request: NullablePlainNestedRequest,
    ): NullablePlainNestedRequest = request

    // Case 10: child nullable=O, child @NotNull=X, child @Valid=X, Controller @Valid=O
    // child 누락/null -> 200, child.age 누락/null -> 200 (cascade X)
    // child.age=0 또는 child.age=25 -> 200 OK
    @PostMapping("/nullable/plain/valid")
    fun nullablePlainValid(
        @Valid @RequestBody request: NullablePlainNestedRequest,
    ): NullablePlainNestedRequest = request

    // Case 11: child nullable=O, child @NotNull=X, child @Valid=O, Controller @Valid=X
    // child 누락/null -> 200, child.age 누락/null -> 200 (검증 미실행)
    // child.age=0 또는 child.age=25 -> 200 OK
    @PostMapping("/nullable/cascade")
    fun nullableCascade(
        @RequestBody request: NullableCascadeNestedRequest,
    ): NullableCascadeNestedRequest = request

    // Case 12: child nullable=O, child @NotNull=X, child @Valid=O, Controller @Valid=O
    // child 누락/null -> 200, child.age 누락/null -> 400, fields=[child.age]
    // child.age=0 또는 child.age=25 -> 200 OK
    @PostMapping("/nullable/cascade/valid")
    fun nullableCascadeValid(
        @Valid @RequestBody request: NullableCascadeNestedRequest,
    ): NullableCascadeNestedRequest = request

    // Case 13: child nullable=O, child @NotNull=O, child @Valid=X, Controller @Valid=X
    // child 누락/null -> 200, child.age 누락/null -> 200 (검증 미실행)
    // child.age=0 또는 child.age=25 -> 200 OK
    @PostMapping("/nullable/not-null")
    fun nullableNotNull(
        @RequestBody request: NullableNotNullNestedRequest,
    ): NullableNotNullNestedRequest = request

    // Case 14: child nullable=O, child @NotNull=O, child @Valid=X, Controller @Valid=O
    // child 누락/null -> 400, fields=[child], child.age 누락/null -> 200 (cascade X)
    // child.age=0 또는 child.age=25 -> 200 OK
    @PostMapping("/nullable/not-null/valid")
    fun nullableNotNullValid(
        @Valid @RequestBody request: NullableNotNullNestedRequest,
    ): NullableNotNullNestedRequest = request

    // Case 15: child nullable=O, child @NotNull=O, child @Valid=O, Controller @Valid=X
    // child 누락/null -> 200, child.age 누락/null -> 200 (검증 미실행)
    // child.age=0 또는 child.age=25 -> 200 OK
    @PostMapping("/nullable/not-null/cascade")
    fun nullableNotNullCascade(
        @RequestBody request: NullableNotNullCascadeNestedRequest,
    ): NullableNotNullCascadeNestedRequest = request

    // Case 16: child nullable=O, child @NotNull=O, child @Valid=O, Controller @Valid=O
    // child 누락/null -> 400, fields=[child], child.age 누락/null -> 400, fields=[child.age]
    // child.age=0 또는 child.age=25 -> 200 OK
    @PostMapping("/nullable/not-null/cascade/valid")
    fun nullableNotNullCascadeValid(
        @Valid @RequestBody request: NullableNotNullCascadeNestedRequest,
    ): NullableNotNullCascadeNestedRequest = request
}
