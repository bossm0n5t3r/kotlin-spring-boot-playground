package me.bossm0n5t3r.mcpserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@ConfigurationPropertiesScan @SpringBootApplication class McpServerApplication

fun main(args: Array<String>) {
    runApplication<McpServerApplication>(*args)
}
