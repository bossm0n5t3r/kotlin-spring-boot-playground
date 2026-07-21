package me.bossm0n5t3r.readability4k.exception

import org.springframework.http.HttpStatus

class ReadabilityException(message: String, val status: HttpStatus) : RuntimeException(message)
