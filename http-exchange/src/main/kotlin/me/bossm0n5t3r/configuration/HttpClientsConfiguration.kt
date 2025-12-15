package me.bossm0n5t3r.configuration

import me.bossm0n5t3r.infrastructure.JsonPlaceHolderProperties
import me.bossm0n5t3r.infrastructure.JsonPlaceHolderRestClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient
import org.springframework.web.client.support.RestClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import org.springframework.web.service.invoker.createClient

@Configuration
class HttpClientsConfiguration {
    @Bean
    fun jsonPlaceholderRestClient(props: JsonPlaceHolderProperties): RestClient =
        RestClient
            .builder()
            .baseUrl(props.baseUrl)
            .build()

    @Bean
    fun jsonPlaceholderRestExchangeClient(restClient: RestClient): JsonPlaceHolderRestClient {
        val adapter = RestClientAdapter.create(restClient)
        val factory = HttpServiceProxyFactory.builder().exchangeAdapter(adapter).build()
        return factory.createClient<JsonPlaceHolderRestClient>()
    }
}
