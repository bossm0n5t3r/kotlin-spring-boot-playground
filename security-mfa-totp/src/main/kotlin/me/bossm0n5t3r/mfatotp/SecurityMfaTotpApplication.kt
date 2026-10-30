package me.bossm0n5t3r.mfatotp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication class SecurityMfaTotpApplication

fun main(args: Array<String>) {
    runApplication<SecurityMfaTotpApplication>(*args)
}
