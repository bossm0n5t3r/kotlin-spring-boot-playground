package me.bossm0n5t3r.configuration

import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.v1.jdbc.Database
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class DatabaseConfiguration {
    @Bean
    @Primary
    @ConfigurationProperties("app.datasource.master.hikari")
    fun masterDataSource() = HikariDataSource()

    @Bean
    @Primary
    fun masterDatabase(
        @Qualifier("masterDataSource") dataSource: HikariDataSource,
    ): Database = Database.connect(dataSource)

    @Bean
    @ConfigurationProperties("app.datasource.slave.hikari")
    fun slaveDataSource() = HikariDataSource()

    @Bean
    fun slaveDatabase(
        @Qualifier("slaveDataSource") dataSource: HikariDataSource,
    ): Database = Database.connect(dataSource)
}
