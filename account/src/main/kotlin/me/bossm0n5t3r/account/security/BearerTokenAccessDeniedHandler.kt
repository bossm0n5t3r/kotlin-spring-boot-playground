package me.bossm0n5t3r.account.security

import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class BearerTokenAccessDeniedHandler : ServerAccessDeniedHandler {
    override fun handle(
        exchange: ServerWebExchange,
        denied: AccessDeniedException,
    ): Mono<Void> =
        with(exchange.response) {
            statusCode = HttpStatus.FORBIDDEN
            setComplete()
        }
}
