package me.bossm0n5t3r.sse

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SseApplication

fun main(args: Array<String>) {
    runApplication<SseApplication>(*args)
}
