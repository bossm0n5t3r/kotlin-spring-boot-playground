package me.bossm0n5t3r.nullsafety.map.nullableconstrained

import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import me.bossm0n5t3r.nullsafety.map.MapChild

data class NonNullPlainValueRequest(
    @field:NotNull val children: Map<String, MapChild>?,
)

data class NonNullCascadeValueRequest(
    @field:NotNull val children: Map<String, @Valid MapChild>?,
)

data class NonNullNotNullValueRequest(
    @field:NotNull val children: Map<String, @NotNull MapChild>?,
)

data class NonNullNotNullCascadeValueRequest(
    @field:NotNull val children: Map<String, @NotNull @Valid MapChild>?,
)

data class NullablePlainValueRequest(
    @field:NotNull val children: Map<String, MapChild?>?,
)

data class NullableCascadeValueRequest(
    @field:NotNull val children: Map<String, @Valid MapChild?>?,
)

data class NullableNotNullValueRequest(
    @field:NotNull val children: Map<String, @NotNull MapChild?>?,
)

data class NullableNotNullCascadeValueRequest(
    @field:NotNull val children: Map<String, @NotNull @Valid MapChild?>?,
)
