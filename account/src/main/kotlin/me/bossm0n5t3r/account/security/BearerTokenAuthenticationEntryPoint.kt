package me.bossm0n5t3r.account.security

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.server.ServerAuthenticationEntryPoint
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class BearerTokenAuthenticationEntryPoint : ServerAuthenticationEntryPoint {
    override fun commence(
        exchange: ServerWebExchange,
        e: AuthenticationException,
    ): Mono<Void> {
        val (status, wwwAuthenticate) =
            when (e) {
                is TokenFormatInvalidException ->
                    HttpStatus.BAD_REQUEST to """Bearer error="invalid_request", error_description="Malformed authorization token""""
                else ->
                    HttpStatus.UNAUTHORIZED to """Bearer error="invalid_token""""
            }

        val response = exchange.response
        response.statusCode = status
        response.headers.set(HttpHeaders.WWW_AUTHENTICATE, wwwAuthenticate)

        return response.setComplete()
    }
}
