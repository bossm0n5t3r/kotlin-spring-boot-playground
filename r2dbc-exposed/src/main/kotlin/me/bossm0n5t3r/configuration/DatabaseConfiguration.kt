package me.bossm0n5t3r.configuration

import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.IsolationLevel
import org.jetbrains.exposed.v1.core.vendors.H2Dialect
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabaseConfig
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class DatabaseConfiguration(
    private val databaseProperties: DatabaseProperties,
) {
    @Bean
    fun r2dbcDatabaseConfig(): R2dbcDatabaseConfig.Builder =
        R2dbcDatabaseConfig {
            defaultMaxAttempts = 1
            defaultR2dbcIsolationLevel = IsolationLevel.READ_COMMITTED
            explicitDialect = H2Dialect()
        }

    @Bean
    @Primary
    fun masterDatabase(
        @Qualifier("r2dbcDatabaseConfig") config: R2dbcDatabaseConfig.Builder,
    ): R2dbcDatabase =
        R2dbcDatabase.connect(
            connectionFactory = ConnectionFactories.get(databaseProperties.master.r2dbc.url),
            databaseConfig = config,
        )

    @Bean
    fun slaveDatabase(
        @Qualifier("r2dbcDatabaseConfig") config: R2dbcDatabaseConfig.Builder,
    ): R2dbcDatabase =
        R2dbcDatabase.connect(
            connectionFactory = ConnectionFactories.get(databaseProperties.slave.r2dbc.url),
            databaseConfig = config,
        )
}
