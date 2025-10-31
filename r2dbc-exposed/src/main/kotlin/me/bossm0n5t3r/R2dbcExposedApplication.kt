package me.bossm0n5t3r

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class R2dbcExposedApplication

fun main(args: Array<String>) {
    runApplication<R2dbcExposedApplication>(*args)
}
