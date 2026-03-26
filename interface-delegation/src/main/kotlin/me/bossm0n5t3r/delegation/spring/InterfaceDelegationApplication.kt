package me.bossm0n5t3r.delegation.spring

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class InterfaceDelegationApplication

fun main(args: Array<String>) {
    runApplication<InterfaceDelegationApplication>(*args)
}
