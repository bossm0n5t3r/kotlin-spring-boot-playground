package me.bossm0n5t3r.readability4k.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(private val properties: ReadabilityProperties) : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry
            .addMapping("/readability4k/**")
            .allowedOrigins(*properties.cors.origins.toTypedArray())
            .allowedMethods("GET")
            .allowedHeaders("*")
    }
}
