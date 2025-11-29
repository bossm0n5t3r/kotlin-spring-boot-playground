package me.bossm0n5t3r.sse

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class SseApplication

fun main(args: Array<String>) {
    runApplication<SseApplication>(*args)
}
