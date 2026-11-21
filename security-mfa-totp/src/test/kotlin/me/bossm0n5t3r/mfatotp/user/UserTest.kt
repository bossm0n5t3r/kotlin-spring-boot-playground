package me.bossm0n5t3r.mfatotp.user

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UserTest {
    private val sut = User("alice", "password")

    @Test
    fun useTotpStep_returns_true_for_first_step() {
        assertTrue(sut.useTotpStep(100))
    }

    @Test
    fun useTotpStep_returns_false_for_already_used_or_older_step() {
        // given
        sut.useTotpStep(100)

        // when, then
        assertFalse(sut.useTotpStep(100))
        assertFalse(sut.useTotpStep(99))
    }

    @Test
    fun useTotpStep_returns_true_for_newer_step() {
        // given
        sut.useTotpStep(100)

        // when, then
        assertTrue(sut.useTotpStep(101))
    }
}
