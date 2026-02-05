package me.bossm0n5t3r.txroutingdatasource.exception

import org.springframework.http.HttpStatus

class NotFoundException(
    message: String? = null,
    cause: Throwable? = null,
) : Exception(message, cause) {
    val httpStatus = HttpStatus.NOT_FOUND

    constructor(cause: Throwable) : this(null, cause)
}
