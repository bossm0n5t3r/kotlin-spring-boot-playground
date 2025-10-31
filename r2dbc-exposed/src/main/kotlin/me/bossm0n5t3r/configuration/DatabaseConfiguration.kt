package me.bossm0n5t3r.configuration

import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactoryOptions
import io.r2dbc.spi.IsolationLevel
import io.r2dbc.spi.Option
import org.jetbrains.exposed.v1.core.vendors.H2Dialect
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabaseConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DatabaseConfiguration {
    @Bean
    fun database(): R2dbcDatabase {
        val options =
            ConnectionFactoryOptions
                .builder()
                .option(ConnectionFactoryOptions.DRIVER, "h2")
                .option(ConnectionFactoryOptions.PROTOCOL, "mem")
                .option(ConnectionFactoryOptions.DATABASE, "test")
                .option(Option.valueOf("DB_CLOSE_DELAY"), "-1")
                .build()

        val connectionFactory = ConnectionFactories.get(options)

        return R2dbcDatabase.connect(
            connectionFactory = connectionFactory,
            databaseConfig =
                R2dbcDatabaseConfig {
                    defaultMaxAttempts = 1
                    defaultR2dbcIsolationLevel = IsolationLevel.READ_COMMITTED
                    explicitDialect = H2Dialect()
                },
        )
    }
}
