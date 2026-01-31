package me.bossm0n5t3r.security.mvc.config

import me.bossm0n5t3r.security.mvc.client.AccountClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient
import org.springframework.web.client.support.RestClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import org.springframework.web.service.invoker.createClient

@Configuration
class AccountClientConfig(
    private val accountProperties: AccountProperties,
) {
    @Bean
    fun accountClient(): AccountClient {
        val restClient =
            RestClient
                .builder()
                .baseUrl(accountProperties.url)
                .build()
        val adapter = RestClientAdapter.create(restClient)
        val factory =
            HttpServiceProxyFactory
                .builderFor(adapter)
                .build()
        return factory.createClient<AccountClient>()
    }
}
