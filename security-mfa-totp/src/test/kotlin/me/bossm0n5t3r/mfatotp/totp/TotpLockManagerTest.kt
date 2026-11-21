package me.bossm0n5t3r.mfatotp.totp

import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import me.bossm0n5t3r.mfatotp.support.MutableClock

class TotpLockManagerTest {
    private val clock = MutableClock()
    private val sut = TotpLockManager(clock)

    @Test
    fun isLocked_returns_false_if_failures_are_less_than_3() {
        // given
        val username = "alice"

        // when
        repeat(2) { sut.recordFailure(username) }

        // then
        assertFalse { sut.isLocked(username) }
    }

    @Test
    fun isLocked_returns_true_after_3_failures() {
        // given
        val username = "alice"

        // when
        repeat(3) { sut.recordFailure(username) }

        // then
        assertTrue { sut.isLocked(username) }
    }

    @Test
    fun isLocked_returns_true_until_1_hour_after_last_failure() {
        // given
        val username = "alice"
        repeat(3) { sut.recordFailure(username) }

        // when
        clock.advance(Duration.ofHours(1).minusNanos(1))

        // then
        assertTrue { sut.isLocked(username) }

        // when
        clock.advance(Duration.ofNanos(1))

        // then
        assertFalse { sut.isLocked(username) }
    }

    @Test
    fun recordFailure_restarts_count_from_1_after_lock_expires() {
        // given
        val username = "alice"
        repeat(3) { sut.recordFailure(username) }
        clock.advance(Duration.ofHours(1))

        // when
        sut.recordFailure(username)

        // then
        assertEquals(1, sut.failureCount(username))
    }

    @Test
    fun resetFailures_unlocks_user() {
        // given
        val username = "alice"
        repeat(3) { sut.recordFailure(username) }

        // when
        sut.resetFailures(username)

        // then
        assertFalse { sut.isLocked(username) }
    }

    @Test
    fun failureCount_is_tracked_separately_per_user() {
        // when
        repeat(3) { sut.recordFailure("alice") }
        repeat(2) { sut.recordFailure("bob") }

        // then
        assertEquals(3, sut.failureCount("alice"))
        assertEquals(2, sut.failureCount("bob"))

        // when
        sut.resetFailures("alice")

        // then
        assertEquals(0, sut.failureCount("alice"))
        assertEquals(2, sut.failureCount("bob"))
    }

    @Test
    fun recordFailure_counts_correctly_under_concurrent_calls() {
        // given
        val username = "alice"
        val threadCount = 8
        val failuresPerThread = 10_000
        val start = CountDownLatch(1)

        // when
        Executors.newFixedThreadPool(threadCount).use { executor ->
            val futures =
                List(threadCount) {
                    executor.submit {
                        start.await()
                        repeat(failuresPerThread) { sut.recordFailure(username) }
                    }
                }
            start.countDown()
            futures.forEach { it.get() }
        }

        // then
        assertEquals(threadCount * failuresPerThread, sut.failureCount(username))
    }
}
