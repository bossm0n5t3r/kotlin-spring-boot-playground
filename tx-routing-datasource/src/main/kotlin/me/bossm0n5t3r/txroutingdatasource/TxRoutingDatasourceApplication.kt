package me.bossm0n5t3r.txroutingdatasource

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication class TxRoutingDatasourceApplication

fun main(args: Array<String>) {
    runApplication<TxRoutingDatasourceApplication>(*args)
}
