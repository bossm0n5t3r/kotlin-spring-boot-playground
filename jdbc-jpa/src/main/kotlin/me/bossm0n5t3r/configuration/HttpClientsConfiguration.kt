package me.bossm0n5t3r.configuration

import me.bossm0n5t3r.infrastructure.http.JsonPlaceholderProperties
import me.bossm0n5t3r.infrastructure.http.JsonPlaceholderRestClient
import me.bossm0n5t3r.infrastructure.http.JsonPlaceholderWebClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient
import org.springframework.web.client.support.RestClientAdapter
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory

@Configuration
class HttpClientsConfiguration {
    @Bean
    fun jsonPlaceholderWebClient(props: JsonPlaceholderProperties): WebClient =
        WebClient
            .builder()
            .baseUrl(props.baseUrl)
            .build()

    @Bean
    fun jsonPlaceholderClient(webClient: WebClient): JsonPlaceholderWebClient {
        val adapter = WebClientAdapter.create(webClient)
        val factory = HttpServiceProxyFactory.builder().exchangeAdapter(adapter).build()
        return factory.createClient(JsonPlaceholderWebClient::class.java)
    }

    @Bean
    fun jsonPlaceholderRestClient(props: JsonPlaceholderProperties): RestClient =
        RestClient
            .builder()
            .baseUrl(props.baseUrl)
            .build()

    @Bean
    fun jsonPlaceholderRestExchangeClient(restClient: RestClient): JsonPlaceholderRestClient {
        val adapter = RestClientAdapter.create(restClient)
        val factory = HttpServiceProxyFactory.builder().exchangeAdapter(adapter).build()
        return factory.createClient(JsonPlaceholderRestClient::class.java)
    }
}
