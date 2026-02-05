package me.bossm0n5t3r.txroutingdatasource

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import me.bossm0n5t3r.txroutingdatasource.config.RoutingDataSource
import me.bossm0n5t3r.txroutingdatasource.config.RoutingDataSourceConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.sql.DataSource

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
    }

    @Entity
    @jakarta.persistence.Table(name = "test_entity")
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

    @Autowired
    lateinit var testService: TestService

    @Autowired
    @Qualifier("masterDataSource")
    lateinit var masterDataSource: DataSource

    @Autowired
    @Qualifier("slaveDataSource")
    lateinit var slaveDataSource: DataSource

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
            masterJdbc().queryForObject(
                "SELECT COUNT(*) FROM test_entity WHERE name = 'master-test'",
                Int::class.java,
            )
        assertThat(count).isEqualTo(1)
        
        // Slave should be empty
        val slaveCount = slaveJdbc().queryForObject("SELECT COUNT(*) FROM test_entity", Int::class.java)
        assertThat(slaveCount).isEqualTo(0)
    }

    @Test
    fun `should route to slave when transactional readOnly is true`() {
        // Manually insert into master
        masterJdbc().execute("INSERT INTO test_entity (name) VALUES ('master-data')")
        
        // Manually insert into slave
        slaveJdbc().execute("INSERT INTO test_entity (name) VALUES ('slave-data')")
        
        // Read (should go to slave)
        // Note: Using findById(1) might be tricky with auto-increment, but since we cleared it, it should be 1.
        val entity = testService.read(1L)
        
        // This is a minimal check. In a real test, we'd verify it actually went to slave.
        // If it returns 'slave-data', it means it read from slave.
        assertThat(entity?.name).isEqualTo("slave-data")
    }

    private fun masterJdbc(): JdbcTemplate = JdbcTemplate(masterDataSource)

    private fun slaveJdbc(): JdbcTemplate = JdbcTemplate(slaveDataSource)
}
