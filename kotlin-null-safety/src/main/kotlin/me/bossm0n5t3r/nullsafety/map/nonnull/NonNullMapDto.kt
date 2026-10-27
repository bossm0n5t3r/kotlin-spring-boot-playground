package me.bossm0n5t3r.nullsafety.map.nonnull

import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import me.bossm0n5t3r.nullsafety.map.MapChild

data class NonNullPlainValueRequest(val children: Map<String, MapChild>)

data class NonNullCascadeValueRequest(val children: Map<String, @Valid MapChild>)

data class NonNullNotNullValueRequest(val children: Map<String, @NotNull MapChild>)

data class NonNullNotNullCascadeValueRequest(val children: Map<String, @NotNull @Valid MapChild>)

data class NullablePlainValueRequest(val children: Map<String, MapChild?>)

data class NullableCascadeValueRequest(val children: Map<String, @Valid MapChild?>)

data class NullableNotNullValueRequest(val children: Map<String, @NotNull MapChild?>)

data class NullableNotNullCascadeValueRequest(val children: Map<String, @NotNull @Valid MapChild?>)
