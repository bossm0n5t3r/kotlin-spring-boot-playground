package me.bossm0n5t3r.contentnegotiation.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter

@Configuration
class WebConfig {
    @Bean
    fun protobufHttpMessageConverter(): ProtobufHttpMessageConverter = ProtobufHttpMessageConverter()

    @Bean
    fun messagePackHttpMessageConverter(): MessagePackHttpMessageConverter = MessagePackHttpMessageConverter()
}
