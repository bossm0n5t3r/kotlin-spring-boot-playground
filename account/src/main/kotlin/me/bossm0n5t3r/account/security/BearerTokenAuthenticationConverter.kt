package me.bossm0n5t3r.account.security

import me.bossm0n5t3r.account.util.Constants.BEARER_PREFIX
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class BearerTokenAuthenticationConverter : ServerAuthenticationConverter {
    override fun convert(exchange: ServerWebExchange): Mono<Authentication> =
        Mono
            .justOrEmpty(exchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION))
            .flatMap { authHeader ->
                validateAndExtractToken(authHeader)
                    .map { token -> UsernamePasswordAuthenticationToken(null, token) }
            }

    private fun validateAndExtractToken(authHeader: String): Mono<String> {
        if (!authHeader.startsWith(BEARER_PREFIX)) {
            return Mono.error(TokenFormatInvalidException("Authorization header must start with Bearer"))
        }

        val token = authHeader.substringAfter(BEARER_PREFIX).trim()

        return when {
            token.isEmpty() -> Mono.error(TokenFormatInvalidException("Token is empty"))
            token.contains(" ") -> Mono.error(TokenFormatInvalidException("Token contains whitespace"))
            else -> Mono.just(token)
        }
    }
}
