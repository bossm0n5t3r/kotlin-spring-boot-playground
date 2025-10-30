package me.bossm0n5t3r.support

import me.bossm0n5t3r.domain.UserEntity
import org.jetbrains.exposed.v1.core.StdOutSqlLogger
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class SchemaInitialize(
    private val database: Database,
) : ApplicationRunner {
    override fun run(args: ApplicationArguments?) =
        transaction(database) {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(UserEntity)
        }
}
