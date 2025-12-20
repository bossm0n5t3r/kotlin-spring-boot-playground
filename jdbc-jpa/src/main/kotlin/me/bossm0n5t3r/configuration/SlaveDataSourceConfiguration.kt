package me.bossm0n5t3r.configuration

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import jakarta.persistence.EntityManagerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jpa.EntityManagerFactoryBuilder
import org.springframework.boot.jpa.autoconfigure.JpaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

@Configuration
@EnableJpaRepositories(
    basePackages = [SlaveDataSourceConfiguration.REPOSITORY_BASE_PACKAGE],
    entityManagerFactoryRef = SlaveDataSourceConfiguration.ENTITY_MANAGER_FACTORY,
    transactionManagerRef = SlaveDataSourceConfiguration.TRANSACTION_MANAGER,
)
class SlaveDataSourceConfiguration {
    companion object {
        const val REPOSITORY_BASE_PACKAGE = "me.bossm0n5t3r.domain.repository.read"
        const val ENTITY_BASE_PACKAGE = "me.bossm0n5t3r.domain.entity"
        const val ENTITY_MANAGER_FACTORY = "slaveEntityManagerFactory"
        const val TRANSACTION_MANAGER = "slaveTransactionManager"
        const val PERSISTENCE_UNIT = "slave"
    }

    @Bean
    @ConfigurationProperties("app.datasource.slave.hikari")
    fun slaveHikariConfig(): HikariConfig = HikariConfig()

    @Bean
    fun slaveDataSource(
        @Qualifier("slaveHikariConfig") slaveHikariConfig: HikariConfig,
    ) = HikariDataSource(slaveHikariConfig)

    @Bean(name = [ENTITY_MANAGER_FACTORY])
    fun slaveEntityManagerFactory(
        @Qualifier("slaveDataSource") dataSource: DataSource,
        builder: EntityManagerFactoryBuilder,
        jpaProperties: JpaProperties,
    ): LocalContainerEntityManagerFactoryBean =
        builder
            .dataSource(dataSource)
            .packages(ENTITY_BASE_PACKAGE)
            .persistenceUnit(PERSISTENCE_UNIT)
            .properties(jpaProperties.properties)
            .build()

    @Bean(name = [TRANSACTION_MANAGER])
    fun slaveTransactionManager(
        @Qualifier("slaveEntityManagerFactory") emf: EntityManagerFactory,
    ): PlatformTransactionManager = JpaTransactionManager(emf)
}
