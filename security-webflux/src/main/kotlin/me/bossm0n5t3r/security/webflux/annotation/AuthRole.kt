package me.bossm0n5t3r.security.webflux.annotation

import me.bossm0n5t3r.security.webflux.enumeration.UserRole

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class AuthRole(
    val requiredToken: Boolean = true,
    val requiredRoles: Array<UserRole> = [],
)
