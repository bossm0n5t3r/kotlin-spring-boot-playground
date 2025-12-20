package me.bossm0n5t3r.support

import kotlinx.coroutines.runBlocking
import me.bossm0n5t3r.domain.UserEntity
import org.jetbrains.exposed.v1.core.StdOutSqlLogger
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class SchemaInitialize(
    @Qualifier("masterDatabase") private val masterDatabase: R2dbcDatabase,
) : ApplicationRunner {
    override fun run(args: ApplicationArguments) =
        runBlocking {
            suspendTransaction(masterDatabase) {
                addLogger(StdOutSqlLogger)
                SchemaUtils.create(UserEntity)
            }
        }
}
