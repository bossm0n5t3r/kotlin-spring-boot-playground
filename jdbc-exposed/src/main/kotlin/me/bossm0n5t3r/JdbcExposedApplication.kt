package me.bossm0n5t3r

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class JdbcExposedApplication

fun main(args: Array<String>) {
    runApplication<JdbcExposedApplication>(*args)
}
