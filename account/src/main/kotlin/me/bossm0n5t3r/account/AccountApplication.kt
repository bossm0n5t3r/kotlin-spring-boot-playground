package me.bossm0n5t3r.account

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

@SpringBootApplication
@EnableR2dbcRepositories
class AccountApplication

fun main(args: Array<String>) {
    runApplication<AccountApplication>(*args)
}
