package me.bossm0n5t3r.contentnegotiation

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ContentNegotiationApplication

fun main(args: Array<String>) {
    runApplication<ContentNegotiationApplication>(*args)
}
