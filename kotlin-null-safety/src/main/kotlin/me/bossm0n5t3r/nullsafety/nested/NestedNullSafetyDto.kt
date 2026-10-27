package me.bossm0n5t3r.nullsafety.nested

import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull

// 자식 내부 제약은 고정하고 부모의 nullability, @NotNull, cascade를 비교합니다.
data class NestedChild(@field:NotNull val age: Int?)

data class NonNullPlainNestedRequest(val child: NestedChild)

data class NonNullCascadeNestedRequest(@field:Valid val child: NestedChild)

data class NonNullNotNullNestedRequest(@field:NotNull val child: NestedChild)

data class NonNullNotNullCascadeNestedRequest(@field:NotNull @field:Valid val child: NestedChild)

data class NullablePlainNestedRequest(val child: NestedChild?)

data class NullableCascadeNestedRequest(@field:Valid val child: NestedChild?)

data class NullableNotNullNestedRequest(@field:NotNull val child: NestedChild?)

data class NullableNotNullCascadeNestedRequest(@field:NotNull @field:Valid val child: NestedChild?)
