package me.bossm0n5t3r.security.mvc.annotation

import me.bossm0n5t3r.security.mvc.enumeration.UserRole

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class AuthRole(
    val requiredToken: Boolean = true,
    val requiredRoles: Array<UserRole> = [],
)
