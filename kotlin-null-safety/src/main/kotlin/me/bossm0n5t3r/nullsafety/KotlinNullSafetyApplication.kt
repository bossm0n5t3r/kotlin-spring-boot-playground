package me.bossm0n5t3r.nullsafety

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication class KotlinNullSafetyApplication

fun main(args: Array<String>) {
    runApplication<KotlinNullSafetyApplication>(*args)
}
