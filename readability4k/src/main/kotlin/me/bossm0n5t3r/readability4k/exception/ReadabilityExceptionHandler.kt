package me.bossm0n5t3r.readability4k.exception

import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ReadabilityExceptionHandler {
    @ExceptionHandler(ReadabilityException::class)
    fun handleReadabilityException(e: ReadabilityException): ResponseEntity<Map<String, String?>> {
        return ResponseEntity.status(e.status).body(mapOf("error" to e.message))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
        e: MethodArgumentNotValidException,
    ): ResponseEntity<Map<String, String?>> {
        val message =
            e.bindingResult.fieldErrors.joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
        return ResponseEntity.badRequest().body(mapOf("error" to message))
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(
        e: ConstraintViolationException,
    ): ResponseEntity<Map<String, String?>> {
        val message =
            e.constraintViolations.joinToString(", ") { "${it.propertyPath}: ${it.message}" }
        return ResponseEntity.badRequest().body(mapOf("error" to message))
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(e: Exception): ResponseEntity<Map<String, String?>> {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(mapOf("error" to (e.message ?: "Unexpected error")))
    }
}
