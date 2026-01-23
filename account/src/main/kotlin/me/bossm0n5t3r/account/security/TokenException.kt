package me.bossm0n5t3r.account.security

import org.springframework.security.core.AuthenticationException

sealed class TokenException(
    message: String,
    cause: Throwable? = null,
) : AuthenticationException(message, cause)

class TokenFormatInvalidException(
    message: String,
    cause: Throwable? = null,
) : TokenException(message, cause)

class TokenValidationFailedException(
    message: String,
    cause: Throwable? = null,
) : TokenException(message, cause)
