package me.bossm0n5t3r.mfatotp.totp

import com.atlassian.onetime.core.HMACDigest
import com.atlassian.onetime.core.OTPLength
import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import me.bossm0n5t3r.mfatotp.support.MutableClock

class TotpLockManagerTest {
    private val clock = MutableClock()
    private val totpProperties =
        TotpProperties(
            allowed = TotpProperties.Allowed(pastSteps = 1, futureSteps = 1),
            startTime = 0,
            timeStepSeconds = 30,
            otpLength = OTPLength.SIX,
            digest = HMACDigest.SHA1,
            lock = TotpProperties.Lock(maxFailures = 3, duration = Duration.ofHours(1)),
        )
    private val sut = TotpLockManager(clock, totpProperties)

    @Test
    fun isLocked_returns_false_if_failures_are_less_than_3() {
        // given
        val username = "alice"

        // when
        repeat(2) { sut.tryRecordAttempt(username) }

        // then
        assertFalse { sut.isLocked(username) }
    }

    @Test
    fun isLocked_returns_true_after_3_failures() {
        // given
        val username = "alice"

        // when
        repeat(3) { sut.tryRecordAttempt(username) }

        // then
        assertTrue { sut.isLocked(username) }
    }

    @Test
    fun isLocked_returns_true_until_1_hour_after_last_failure() {
        // given
        val username = "alice"
        repeat(3) { sut.tryRecordAttempt(username) }

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
    fun tryRecordAttempt_restarts_count_from_1_after_lock_expires() {
        // given
        val username = "alice"
        repeat(3) { sut.tryRecordAttempt(username) }
        clock.advance(Duration.ofHours(1))

        // when
        sut.tryRecordAttempt(username)

        // then
        assertEquals(1, sut.failureCount(username))
    }

    @Test
    fun resetFailures_unlocks_user() {
        // given
        val username = "alice"
        repeat(3) { sut.tryRecordAttempt(username) }

        // when
        sut.resetFailures(username)

        // then
        assertFalse { sut.isLocked(username) }
    }

    @Test
    fun failureCount_is_tracked_separately_per_user() {
        // when
        repeat(3) { sut.tryRecordAttempt("alice") }
        repeat(2) { sut.tryRecordAttempt("bob") }

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
    fun tryRecordAttempt_returns_false_without_counting_if_locked() {
        // given
        val username = "alice"
        repeat(3) { assertTrue { sut.tryRecordAttempt(username) } }

        // when
        val recorded = sut.tryRecordAttempt(username)

        // then
        assertFalse { recorded }
        assertEquals(3, sut.failureCount(username))
    }

    @Test
    fun tryRecordAttempt_allows_only_3_attempts_under_concurrent_calls() {
        // given
        val username = "alice"
        val threadCount = 8
        val attemptsPerThread = 10_000
        val recordedCount = AtomicInteger()
        val start = CountDownLatch(1)

        // when
        Executors.newFixedThreadPool(threadCount).use { executor ->
            val futures =
                List(threadCount) {
                    executor.submit {
                        start.await()
                        repeat(attemptsPerThread) {
                            if (sut.tryRecordAttempt(username)) recordedCount.incrementAndGet()
                        }
                    }
                }
            start.countDown()
            futures.forEach { it.get() }
        }

        // then
        assertEquals(3, recordedCount.get())
        assertEquals(3, sut.failureCount(username))
    }
}
