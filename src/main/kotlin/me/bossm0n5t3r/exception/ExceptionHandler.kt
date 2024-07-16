package me.bossm0n5t3r.exception

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExceptionHandler {
    @ExceptionHandler(NotFoundException::class)
    @ResponseBody
    fun handleApiException(e: NotFoundException): ResponseEntity<String> = ResponseEntity(e.message, e.httpStatus)
}
