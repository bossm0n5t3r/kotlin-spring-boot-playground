package me.bossm0n5t3r.txroutingdatasource

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import me.bossm0n5t3r.txroutingdatasource.config.DataSourceType
import me.bossm0n5t3r.txroutingdatasource.config.RoutingDataSource
import me.bossm0n5t3r.txroutingdatasource.config.RoutingDataSourceConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import javax.sql.DataSource
import org.springframework.jdbc.core.queryForObject

@SpringBootTest(
    classes = [RoutingDataSourceTest.TestApp::class],
)
class RoutingDataSourceTest {

    @Import(RoutingDataSourceConfig::class)
    @EnableAutoConfiguration
    @org.springframework.context.annotation.Configuration
    class TestApp {
        @Bean
        fun testService(testRepository: TestRepository): TestService {
            return TestService(testRepository)
        }

        @Bean
        fun routingKeyProbe(
            @Qualifier("routingDataSource") routingDataSource: RoutingDataSource,
        ): RoutingKeyProbe {
            return RoutingKeyProbe(routingDataSource)
        }

        @Bean
        fun innerTxService(routingKeyProbe: RoutingKeyProbe): InnerTxService {
            return InnerTxService(routingKeyProbe)
        }

        @Bean
        fun outerTxService(
            routingKeyProbe: RoutingKeyProbe,
            innerTxService: InnerTxService,
        ): OuterTxService {
            return OuterTxService(routingKeyProbe, innerTxService)
        }
    }

    @Entity
    @Table(name = "test_entity")
    class TestEntity(
        @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
        val id: Long? = null,
        val name: String
    )

    @Repository
    interface TestRepository : JpaRepository<TestEntity, Long>

    @Service
    class TestService(
        private val testRepository: TestRepository
    ) {
        @Transactional(readOnly = false)
        fun write(name: String): TestEntity {
            return testRepository.save(TestEntity(name = name))
        }

        @Transactional(readOnly = true)
        fun read(id: Long): TestEntity? {
            return testRepository.findById(id).orElse(null)
        }
    }

    class RoutingKeyProbe(
        private val routingDataSource: RoutingDataSource,
    ) {
        fun currentKey(): DataSourceType {
            val method = RoutingDataSource::class.java.getDeclaredMethod("determineCurrentLookupKey")
            method.isAccessible = true
            return method.invoke(routingDataSource) as DataSourceType
        }
    }

    open class InnerTxService(
        private val routingKeyProbe: RoutingKeyProbe,
    ) {
        @Transactional(readOnly = false, propagation = Propagation.REQUIRES_NEW)
        open fun writeNew(): DataSourceType = routingKeyProbe.currentKey()

        @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
        open fun readNew(): DataSourceType = routingKeyProbe.currentKey()

        @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
        open fun readRequired(): DataSourceType = routingKeyProbe.currentKey()
    }

    open class OuterTxService(
        private val routingKeyProbe: RoutingKeyProbe,
        private val innerTxService: InnerTxService,
    ) {
        @Transactional(readOnly = true)
        open fun outerReadOnlyInnerWriteNew(): Pair<DataSourceType, DataSourceType> {
            val outer = routingKeyProbe.currentKey()
            val inner = innerTxService.writeNew()
            return outer to inner
        }

        @Transactional(readOnly = false)
        open fun outerWriteInnerReadNew(): Pair<DataSourceType, DataSourceType> {
            val outer = routingKeyProbe.currentKey()
            val inner = innerTxService.readNew()
            return outer to inner
        }

        @Transactional(readOnly = false)
        open fun outerWriteInnerReadRequired(): Pair<DataSourceType, DataSourceType> {
            val outer = routingKeyProbe.currentKey()
            val inner = innerTxService.readRequired()
            return outer to inner
        }
    }

    @Autowired
    lateinit var testService: TestService

    @Autowired
    @Qualifier("masterDataSource")
    lateinit var masterDataSource: DataSource

    @Autowired
    @Qualifier("slaveDataSource")
    lateinit var slaveDataSource: DataSource

    @Autowired
    lateinit var routingDataSource: RoutingDataSource

    @Autowired
    lateinit var transactionManager: PlatformTransactionManager

    @Autowired
    lateinit var outerTxService: OuterTxService

    @BeforeEach
    fun setup() {
        masterJdbc().execute("DELETE FROM test_entity")
        slaveJdbc().execute("DELETE FROM test_entity")
    }

    @Test
    fun `should route to master when transactional readOnly is false`() {
        testService.write("master-test")
        
        // Master
        val count =
            masterJdbc().queryForObject<Int>(
                "SELECT COUNT(*) FROM test_entity WHERE name = 'master-test'",
            )
        assertThat(count).isEqualTo(1)
        
        // Slave should be empty
        val slaveCount = slaveJdbc().queryForObject<Int>("SELECT COUNT(*) FROM test_entity")
        assertThat(slaveCount).isEqualTo(0)
    }

    @Test
    fun `should route to slave when transactional readOnly is true`() {
        masterJdbc().execute("INSERT INTO test_entity (id, name) VALUES (1, 'master-data')")
        slaveJdbc().execute("INSERT INTO test_entity (id, name) VALUES (1, 'slave-data')")

        val entity = testService.read(1L)

        assertThat(entity?.name).isEqualTo("slave-data")
    }

    @Test
    fun `should determine routing key by transaction readOnly flag`() {
        val readOnlyKey =
            TransactionTemplate(transactionManager)
                .apply { isReadOnly = true }
                .execute { routingDataSource.currentLookupKey() }

        val writeKey =
            TransactionTemplate(transactionManager)
                .apply { isReadOnly = false }
                .execute { routingDataSource.currentLookupKey() }

        assertThat(readOnlyKey).isEqualTo(DataSourceType.SLAVE)
        assertThat(writeKey).isEqualTo(DataSourceType.MASTER)
    }

    @Test
    fun `should route based on nested transaction propagation and readOnly`() {
        val (outerReadOnly, innerWriteNew) = outerTxService.outerReadOnlyInnerWriteNew()
        assertThat(outerReadOnly).isEqualTo(DataSourceType.SLAVE)
        assertThat(innerWriteNew).isEqualTo(DataSourceType.MASTER)

        val (outerWrite, innerReadNew) = outerTxService.outerWriteInnerReadNew()
        assertThat(outerWrite).isEqualTo(DataSourceType.MASTER)
        assertThat(innerReadNew).isEqualTo(DataSourceType.SLAVE)

        val (outerWrite2, innerReadRequired) = outerTxService.outerWriteInnerReadRequired()
        assertThat(outerWrite2).isEqualTo(DataSourceType.MASTER)
        assertThat(innerReadRequired).isEqualTo(DataSourceType.MASTER)
    }

    private fun RoutingDataSource.currentLookupKey(): Any {
        val method = RoutingDataSource::class.java.getDeclaredMethod("determineCurrentLookupKey")
        method.isAccessible = true
        return method.invoke(this)
    }

    private fun masterJdbc(): JdbcTemplate = JdbcTemplate(masterDataSource)

    private fun slaveJdbc(): JdbcTemplate = JdbcTemplate(slaveDataSource)
}
