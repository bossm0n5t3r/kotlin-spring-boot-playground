package me.bossm0n5t3r.mfatotp.user

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.uuid.Uuid
import org.junit.jupiter.api.assertThrows
import org.springframework.security.core.userdetails.UsernameNotFoundException

class UserStoreTest {
    private val sut = UserStore()

    @Test
    fun createUserOrNull_returns_created_user() {
        // given
        val username = Uuid.random().toHexDashString()
        val hashedPassword = Uuid.random().toHexDashString()

        // when
        val userOrNull = sut.createUserOrNull(username, hashedPassword)

        // then
        assertNotNull(userOrNull)
        assertEquals(username, userOrNull.username)
        assertEquals(hashedPassword, userOrNull.password)
    }

    @Test
    fun createUserOrNull_returns_null_and_keeps_existing_user_if_username_already_exists() {
        // given
        val username = Uuid.random().toHexDashString()
        val hashedPassword = Uuid.random().toHexDashString()
        sut.createUserOrNull(username, hashedPassword)

        // when
        val anotherHashedPassword = Uuid.random().toHexDashString()
        val userOrNull = sut.createUserOrNull(username, anotherHashedPassword)

        // then
        assertNull(userOrNull)
        val previousUser = sut.loadUserByUsername(username)
        assertEquals(hashedPassword, previousUser.password)
        assertNotEquals(anotherHashedPassword, previousUser.password)
    }

    @Test
    fun loadUserByUsername_returns_user_details() {
        // given
        val username = Uuid.random().toHexDashString()
        val hashedPassword = Uuid.random().toHexDashString()
        sut.createUserOrNull(username, hashedPassword)

        // when
        val userDetails = sut.loadUserByUsername(username)

        // then
        assertEquals(username, userDetails.username)
        assertEquals(hashedPassword, userDetails.password)
    }

    @Test
    fun loadUserByUsername_throws_UsernameNotFoundException_if_user_not_found() {
        // given
        val username = Uuid.random().toHexDashString()

        // when, then
        assertThrows<UsernameNotFoundException> { sut.loadUserByUsername(username) }
    }
}
