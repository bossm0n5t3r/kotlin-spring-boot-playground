package me.bossm0n5t3r.nullsafety

import jakarta.validation.constraints.NotNull

data class NonNullAgeRequest(val age: Int)

data class NonNullNotNullAgeRequest(@field:NotNull val age: Int)

data class NullableAgeRequest(val age: Int?)

data class NullableNotNullAgeRequest(@field:NotNull val age: Int?)

data class AgeResponse(val age: Int?)

data class ValidationErrorResponse(val fields: List<String>)
