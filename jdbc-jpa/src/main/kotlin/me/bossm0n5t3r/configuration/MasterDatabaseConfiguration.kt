package me.bossm0n5t3r.configuration

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import jakarta.persistence.EntityManagerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import javax.sql.DataSource

@Configuration
@EnableJpaRepositories(
    basePackages = [MasterDatabaseConfiguration.REPOSITORY_BASE_PACKAGE],
    entityManagerFactoryRef = MasterDatabaseConfiguration.ENTITY_MANAGER_FACTORY,
    transactionManagerRef = MasterDatabaseConfiguration.TRANSACTION_MANAGER,
)
class MasterDatabaseConfiguration {
    companion object {
        private const val REPOSITORY_BASE_PACKAGE = "me.bossm0n5t3r.domain.repository.write"
        private const val ENTITY_BASE_PACKAGE = "me.bossm0n5t3r.domain.entity"
        private const val ENTITY_MANAGER_FACTORY = "masterEntityManagerFactory"
        private const val TRANSACTION_MANAGER = "masterTransactionManager"
        private const val PERSISTENCE_UNIT = "master"
    }

    @Bean
    @ConfigurationProperties("app.datasource.master.hikari")
    fun masterHikariConfig(): HikariConfig = HikariConfig()

    @Primary
    @Bean
    fun masterDataSource(
        @Qualifier("masterHikariConfig") masterHikariConfig: HikariConfig,
    ): DataSource = HikariDataSource(masterHikariConfig)

    @Primary
    @Bean(name = [ENTITY_MANAGER_FACTORY])
    fun masterEntityManagerFactory(
        @Qualifier("masterDataSource") dataSource: DataSource,
        builder: EntityManagerFactoryBuilder,
        jpaProperties: JpaProperties,
    ): LocalContainerEntityManagerFactoryBean =
        builder
            .dataSource(dataSource)
            .packages(ENTITY_BASE_PACKAGE)
            .persistenceUnit(PERSISTENCE_UNIT)
            .properties(jpaProperties.properties)
            .build()

    @Primary
    @Bean(name = [TRANSACTION_MANAGER])
    fun masterTransactionManager(
        @Qualifier("masterEntityManagerFactory") emf: EntityManagerFactory,
    ): PlatformTransactionManager = JpaTransactionManager(emf)

    @Primary
    @Bean
    fun masterJdbcTemplate(
        @Qualifier("masterDataSource") dataSource: DataSource,
    ): JdbcTemplate = JdbcTemplate(dataSource)
}
