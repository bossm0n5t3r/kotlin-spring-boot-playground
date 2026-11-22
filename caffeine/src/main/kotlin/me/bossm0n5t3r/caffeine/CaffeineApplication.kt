package me.bossm0n5t3r.caffeine

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication class CaffeineApplication

fun main(args: Array<String>) {
    runApplication<CaffeineApplication>(*args)
}
