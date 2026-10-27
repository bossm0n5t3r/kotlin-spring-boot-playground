package me.bossm0n5t3r.nullsafety.scalar

import jakarta.validation.Valid
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/null-safety/ages", consumes = [MediaType.APPLICATION_JSON_VALUE])
class NullSafetyController {
    // Case 1: nullable=X, @NotNull=X, @Valid=X
    // 누락/null -> 400 (Jackson 역직렬화 거부), age=0 또는 age=25 -> 200 OK
    @PostMapping("/non-null/plain")
    fun nonNullPlain(@RequestBody request: NonNullAgeRequest): AgeResponse =
        AgeResponse(request.age)

    // Case 2: nullable=X, @NotNull=X, @Valid=O
    // 누락/null -> 400 (Jackson 역직렬화 거부), age=0 또는 age=25 -> 200 OK
    @PostMapping("/non-null/plain/valid")
    fun nonNullPlainValid(@Valid @RequestBody request: NonNullAgeRequest): AgeResponse =
        AgeResponse(request.age)

    // Case 3: nullable=X, @NotNull=O, @Valid=X
    // 누락/null -> 400 (Jackson 역직렬화 거부), age=0 또는 age=25 -> 200 OK
    @PostMapping("/non-null/not-null")
    fun nonNullNotNull(@RequestBody request: NonNullNotNullAgeRequest): AgeResponse =
        AgeResponse(request.age)

    // Case 4: nullable=X, @NotNull=O, @Valid=O
    // 누락/null -> 400 (Jackson 역직렬화 거부), age=0 또는 age=25 -> 200 OK
    @PostMapping("/non-null/not-null/valid")
    fun nonNullNotNullValid(@Valid @RequestBody request: NonNullNotNullAgeRequest): AgeResponse =
        AgeResponse(request.age)

    // Case 5: nullable=O, @NotNull=X, @Valid=X
    // 누락/null -> 200, {"age":null} (null 허용), age=0 또는 age=25 -> 200 OK
    @PostMapping("/nullable/plain")
    fun nullablePlain(@RequestBody request: NullableAgeRequest): AgeResponse =
        AgeResponse(request.age)

    // Case 6: nullable=O, @NotNull=X, @Valid=O
    // 누락/null -> 200, {"age":null} (검증 제약 X), age=0 또는 age=25 -> 200 OK
    @PostMapping("/nullable/plain/valid")
    fun nullablePlainValid(@Valid @RequestBody request: NullableAgeRequest): AgeResponse =
        AgeResponse(request.age)

    // Case 7: nullable=O, @NotNull=O, @Valid=X
    // 누락/null -> 200, {"age":null} (검증 미실행), age=0 또는 age=25 -> 200 OK
    @PostMapping("/nullable/not-null")
    fun nullableNotNull(@RequestBody request: NullableNotNullAgeRequest): AgeResponse =
        AgeResponse(request.age)

    // Case 8: nullable=O, @NotNull=O, @Valid=O
    // 누락/null -> 400, {"fields":["age"]} (Bean Validation 거부), age=0 또는 age=25 -> 200 OK
    @PostMapping("/nullable/not-null/valid")
    fun nullableNotNullValid(@Valid @RequestBody request: NullableNotNullAgeRequest): AgeResponse =
        AgeResponse(request.age)
}
