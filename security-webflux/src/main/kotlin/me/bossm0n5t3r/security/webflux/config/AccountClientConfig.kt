package me.bossm0n5t3r.security.webflux.config

import me.bossm0n5t3r.security.webflux.client.AccountClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import org.springframework.web.service.invoker.createClient

@Configuration
class AccountClientConfig(
    private val accountProperties: AccountProperties,
) {
    @Bean
    fun accountClient(): AccountClient {
        val accountWebClient =
            WebClient
                .builder()
                .baseUrl(accountProperties.url)
                .build()
        val adapter = WebClientAdapter.create(accountWebClient)
        val factory =
            HttpServiceProxyFactory
                .builderFor(adapter)
                .build()
        return factory.createClient<AccountClient>()
    }
}
