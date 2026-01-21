package me.bossm0n5t3r.security.webflux.exception

import me.bossm0n5t3r.security.webflux.enumeration.ResponseStatus

sealed class SecurityWebfluxException(
    val status: ResponseStatus,
    message: String? = null,
    cause: Throwable? = null,
) : RuntimeException(message ?: status.name, cause)

class AuthTokenRequiredException(
    message: String? = null,
    cause: Throwable? = null,
) : SecurityWebfluxException(ResponseStatus.AUTH_TOKEN_REQUIRED, message, cause)

class UserRoleRestrictedException(
    message: String? = null,
    cause: Throwable? = null,
) : SecurityWebfluxException(ResponseStatus.USER_ROLE_RESTRICTED, message, cause)
