package me.bossm0n5t3r.security.webflux.config

import me.bossm0n5t3r.security.webflux.client.AccountClient
import me.bossm0n5t3r.security.webflux.context.ReactiveUserContext
import me.bossm0n5t3r.security.webflux.context.ReactiveUserContext.ATTR_START_MILLIS
import me.bossm0n5t3r.security.webflux.context.ReactiveUserContext.ATTR_TOKEN_KEY
import me.bossm0n5t3r.security.webflux.context.ReactiveUserContext.ATTR_USER_KEY
import me.bossm0n5t3r.security.webflux.dto.UserDetail
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class AuthWebFilter(
    private val accountClient: AccountClient,
) : WebFilter {
    override fun filter(
        exchange: ServerWebExchange,
        chain: WebFilterChain,
    ): Mono<Void> {
        val startMillis = System.currentTimeMillis()
        val userToken = exchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION) ?: return chain.filter(exchange)

        return accountClient
            .getMe(userToken)
            .flatMap { userAccount ->
                val userDetail =
                    UserDetail(
                        userAccount.id.toString(),
                        roles = listOf(userAccount.role),
                    )
                exchange.attributes[ATTR_START_MILLIS] = startMillis
                exchange.attributes[ATTR_TOKEN_KEY] = userToken
                exchange.attributes[ATTR_USER_KEY] = userAccount

                chain.filter(exchange).contextWrite { ctx ->
                    ReactiveUserContext.putAll(ctx, startMillis, userDetail, userToken)
                }
            }.doOnSuccess {
                val req = exchange.request
                val res = exchange.response
                val elapsedMillis = System.currentTimeMillis() - startMillis

                LOGGER.info(
                    "method: {}, path: {}, status: {}, elapsedMillis: {}",
                    req.method,
                    req.path,
                    res.statusCode?.value(),
                    elapsedMillis,
                )
            }
    }
}
