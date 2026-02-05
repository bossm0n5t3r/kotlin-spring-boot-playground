package me.bossm0n5t3r.txroutingdatasource.config

import com.zaxxer.hikari.HikariDataSource
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.ApplicationContext
import org.springframework.core.io.Resource
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy
import org.springframework.jdbc.datasource.init.DataSourceInitializer
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator
import org.springframework.beans.factory.annotation.Value
import javax.sql.DataSource

@Configuration
class RoutingDataSourceConfig(
    private val applicationContext: ApplicationContext,
    @param:Value("\${spring.sql.init.schema-locations:classpath:schema.sql}")
    private val schemaLocations: String,
) {

    @Bean
    @ConditionalOnMissingBean(name = ["masterDataSource"])
    @ConfigurationProperties(prefix = "spring.datasource.master.hikari")
    fun masterDataSource(): DataSource {
        return HikariDataSource()
    }

    @Bean
    @ConditionalOnMissingBean(name = ["slaveDataSource"])
    @ConfigurationProperties(prefix = "spring.datasource.slave.hikari")
    fun slaveDataSource(): DataSource {
        return HikariDataSource()
    }

    @Bean
    @ConditionalOnMissingBean(name = ["routingDataSource"])
    fun routingDataSource(
        @Qualifier("masterDataSource") masterDataSource: DataSource,
        @Qualifier("slaveDataSource") slaveDataSource: DataSource,
    ): DataSource {
        val routingDataSource = RoutingDataSource()
        val dataSourceMap = mutableMapOf<Any, Any>(
            DataSourceType.MASTER to masterDataSource,
            DataSourceType.SLAVE to slaveDataSource,
        )

        routingDataSource.setTargetDataSources(dataSourceMap)
        routingDataSource.setDefaultTargetDataSource(masterDataSource)

        return routingDataSource
    }

    @Primary
    @Bean
    fun dataSource(@Qualifier("routingDataSource") routingDataSource: DataSource): DataSource {
        return LazyConnectionDataSourceProxy(routingDataSource)
    }

    @Bean
    fun masterDataSourceInitializer(
        @Qualifier("masterDataSource") dataSource: DataSource,
    ): DataSourceInitializer {
        val initializer = DataSourceInitializer()
        initializer.setDataSource(dataSource)
        initializer.setDatabasePopulator(ResourceDatabasePopulator(*schemaResources()))
        return initializer
    }

    @Bean
    fun slaveDataSourceInitializer(
        @Qualifier("slaveDataSource") dataSource: DataSource,
    ): DataSourceInitializer {
        val initializer = DataSourceInitializer()
        initializer.setDataSource(dataSource)
        initializer.setDatabasePopulator(ResourceDatabasePopulator(*schemaResources()))
        return initializer
    }

    private fun schemaResources(): Array<Resource> =
        schemaLocations
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .flatMap { location -> applicationContext.getResources(location).toList() }
            .toTypedArray()
}
