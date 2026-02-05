package me.bossm0n5t3r.txroutingdatasource.service

import me.bossm0n5t3r.txroutingdatasource.TxRoutingDatasourceApplication
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import javax.sql.DataSource

@SpringBootTest(classes = [TxRoutingDatasourceApplication::class])
class UserServiceTest {

    @Autowired
    lateinit var userService: UserService

    @Autowired
    @Qualifier("masterDataSource")
    lateinit var masterDataSource: DataSource

    @Autowired
    @Qualifier("slaveDataSource")
    lateinit var slaveDataSource: DataSource

    @BeforeEach
    fun setup() {
        masterJdbc().execute("DELETE FROM users")
        slaveJdbc().execute("DELETE FROM users")
    }

    @Test
    fun `should save user to master datasource`() {
        // given
        val name = "Master User"
        val email = "master@example.com"

        // when
        val savedUser = userService.createUser(name, email)

        // then
        assertThat(savedUser.id).isNotNull

        // Verify Master
        val masterCount =
            masterJdbc().queryForObject(
                "SELECT COUNT(*) FROM users WHERE name = ?",
                Int::class.java,
                name,
            )
        assertThat(masterCount).isEqualTo(1)

        // Verify Slave (should be 0)
        val slaveCount = slaveJdbc().queryForObject("SELECT COUNT(*) FROM users", Int::class.java)
        assertThat(slaveCount).isEqualTo(0)
    }

    @Test
    fun `should read user from slave datasource`() {
        // given
        val name = "Slave User"
        val email = "slave@example.com"

        // Manually insert into slave
        slaveJdbc().execute("INSERT INTO users (id, name, email) VALUES (1, '$name', '$email')")

        // when
        val user = userService.getUser(1L)

        // then
        assertThat(user).isNotNull
        assertThat(user?.name).isEqualTo(name)

        // Verify Master (should be 0)
        val masterCount = masterJdbc().queryForObject("SELECT COUNT(*) FROM users", Int::class.java)
        assertThat(masterCount).isEqualTo(0)
    }

    @Test
    fun `should update user on master datasource`() {
        // given
        val name = "Before Update"
        val email = "before@example.com"
        val savedUser = userService.createUser(name, email)

        // when
        userService.updateUser(savedUser.id!!, "After Update", null)

        // then
        val updatedName =
            masterJdbc().queryForObject(
                "SELECT name FROM users WHERE id = ?",
                String::class.java,
                savedUser.id,
            )
        assertThat(updatedName).isEqualTo("After Update")
    }

    @Test
    fun `should delete user on master datasource`() {
        // given
        val savedUser = userService.createUser("Delete User", "delete@example.com")

        // when
        userService.deleteUser(savedUser.id!!)

        // then
        val count =
            masterJdbc().queryForObject(
                "SELECT COUNT(*) FROM users WHERE id = ?",
                Int::class.java,
                savedUser.id,
            )
        assertThat(count).isEqualTo(0)
    }

    private fun masterJdbc(): JdbcTemplate = JdbcTemplate(masterDataSource)

    private fun slaveJdbc(): JdbcTemplate = JdbcTemplate(slaveDataSource)
}
