package me.bossm0n5t3r.readability4k

import me.bossm0n5t3r.readability4k.config.ReadabilityProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(ReadabilityProperties::class)
class Readability4kApplication

fun main(args: Array<String>) {
    runApplication<Readability4kApplication>(*args)
}
