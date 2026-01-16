package me.bossm0n5t3r.security.webflux

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SecurityWebfluxApplication

fun main(args: Array<String>) {
    runApplication<SecurityWebfluxApplication>(*args)
}
