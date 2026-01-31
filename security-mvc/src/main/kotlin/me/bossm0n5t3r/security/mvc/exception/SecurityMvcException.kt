package me.bossm0n5t3r.security.mvc.exception

import me.bossm0n5t3r.security.mvc.enumeration.ResponseStatus

sealed class SecurityMvcException(
    val status: ResponseStatus,
    message: String? = null,
    cause: Throwable? = null,
) : RuntimeException(message ?: status.name, cause)

class AuthTokenRequiredException(
    message: String? = null,
    cause: Throwable? = null,
) : SecurityMvcException(ResponseStatus.AUTH_TOKEN_REQUIRED, message, cause)

class UserRoleRestrictedException(
    message: String? = null,
    cause: Throwable? = null,
) : SecurityMvcException(ResponseStatus.USER_ROLE_RESTRICTED, message, cause)
