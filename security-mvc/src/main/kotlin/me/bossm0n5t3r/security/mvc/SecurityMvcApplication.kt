package me.bossm0n5t3r.security.mvc

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@ConfigurationPropertiesScan
@SpringBootApplication
class SecurityMvcApplication

fun main(args: Array<String>) {
    runApplication<SecurityMvcApplication>(*args)
}
