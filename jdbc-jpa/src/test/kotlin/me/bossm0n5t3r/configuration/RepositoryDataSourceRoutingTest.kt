package me.bossm0n5t3r.configuration

import jakarta.persistence.EntityManager
import jakarta.persistence.EntityManagerFactory
import me.bossm0n5t3r.domain.repository.read.UserReadRepository
import me.bossm0n5t3r.domain.repository.write.UserWriteRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.util.AopTestUtils

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RepositoryDataSourceRoutingTest {
    @Autowired
    private lateinit var userReadRepository: UserReadRepository

    @Autowired
    private lateinit var userWriteRepository: UserWriteRepository

    @Autowired
    @Qualifier("masterEntityManagerFactory")
    private lateinit var masterEmf: EntityManagerFactory

    @Autowired
    @Qualifier("slaveEntityManagerFactory")
    private lateinit var slaveEmf: EntityManagerFactory

    @Test
    @DisplayName("UserWriteRepository should be wired to MASTER EntityManagerFactory")
    fun writeRepositoryUsesMasterEmf() {
        val target = AopTestUtils.getTargetObject<Any>(userWriteRepository)
        // SimpleJpaRepository has a private field "em" (EntityManager)
        val em = extractEntityManagerFromRepository(target)
        val actualEmf = em.entityManagerFactory

        assertThat(actualEmf)
            .withFailMessage("Expected write repository to use master EMF, but it did not")
            .isSameAs(masterEmf)
    }

    @Test
    @DisplayName("UserReadRepository should be wired to SLAVE EntityManagerFactory")
    fun readRepositoryUsesSlaveEmf() {
        val target = AopTestUtils.getTargetObject<Any>(userReadRepository)
        val em = extractEntityManagerFromRepository(target)
        val actualEmf = em.entityManagerFactory

        assertThat(actualEmf)
            .withFailMessage("Expected read repository to use slave EMF, but it did not")
            .isSameAs(slaveEmf)
    }

    private fun extractEntityManagerFromRepository(repoImpl: Any): EntityManager {
        // Find a field named "em" or "entityManager" reflectively
        val possibleFields = listOf("em", "entityManager")
        val field =
            possibleFields.firstNotNullOfOrNull { name ->
                runCatching { repoImpl.javaClass.getDeclaredField(name) }.getOrNull()
            }
        requireNotNull(field) { "Could not find EntityManager field in repository implementation: ${repoImpl.javaClass.name}" }

        field.isAccessible = true

        val value = requireNotNull(field.get(repoImpl)) { "EntityManager field was null in ${repoImpl.javaClass.name}" }
        require(value is EntityManager) { "Field ${field.name} is not an EntityManager in ${repoImpl.javaClass.name}" }

        return value
    }
}
