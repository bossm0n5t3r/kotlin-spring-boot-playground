package me.bossm0n5t3r.mfatotp.user

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.uuid.Uuid
import me.bossm0n5t3r.mfatotp.user.dto.CreateUserDto
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.server.ResponseStatusException

class UserServiceTest {
    private val passwordEncoder = BCryptPasswordEncoder()
    private val userStore = UserStore()
    private val sut = UserService(passwordEncoder, userStore)

    @Test
    fun createUser_hashes_password() {
        // given
        val createUserDto = generateRandomCreateUserDto()

        // when
        val result = sut.createUser(createUserDto)

        // then
        assertNotEquals(createUserDto.password, result.password)
        assertTrue(passwordEncoder.matches(createUserDto.password, result.password))
    }

    @Test
    fun createUser_throws_409_if_username_already_exists() {
        // given
        val createUserDto = generateRandomCreateUserDto()
        sut.createUser(createUserDto)

        // when, then
        val exception = assertThrows<ResponseStatusException> { sut.createUser(createUserDto) }
        assertEquals(HttpStatusCode.valueOf(HttpStatus.CONFLICT.value()), exception.statusCode)
    }

    private fun generateRandomCreateUserDto() =
        CreateUserDto(
            username = Uuid.random().toHexDashString(),
            password = Uuid.random().toHexDashString(),
        )
}
